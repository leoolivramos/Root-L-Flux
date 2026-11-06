import urllib.request
import urllib.error
import json
import time

BASE_URL = "http://localhost:8080"

def get(path):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode('utf-8'))
        except Exception:
            return e.code, {}
    except Exception as e:
        return None, str(e)

def post(path, body=None):
    url = f"{BASE_URL}{path}"
    data = json.dumps(body).encode('utf-8') if body is not None else b""
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json", "Accept": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            text = resp.read().decode('utf-8')
            try:
                return resp.status, json.loads(text)
            except Exception:
                return resp.status, text
    except urllib.error.HTTPError as e:
        text = e.read().decode('utf-8')
        try:
            return e.code, json.loads(text)
        except Exception:
            return e.code, text
    except Exception as e:
        return None, str(e)

def get_metric_value(metric_name):
    status, data = get(f"/actuator/metrics/{metric_name}")
    if status == 200 and "measurements" in data:
        for m in data["measurements"]:
            if m.get("statistic") in ("COUNT", "TOTAL", "VALUE"):
                return m.get("value", 0.0)
        return data["measurements"][0].get("value", 0.0)
    return 0.0

print("=" * 65)
print("1. ESTADO INICIAL DA DEAD LETTER QUEUE (DLQ) E MÉTRICAS")
print("=" * 65)

dlq_status, dlq_itens_antes = get("/api/v1/dlq")
dlq_metric_antes = get_metric_value("flux.dlq.messages")
reproc_metric_antes = get_metric_value("flux.dlq.reprocessed")

print(f"  Mensagens retidas na DLQ (/api/v1/dlq): {len(dlq_itens_antes) if isinstance(dlq_itens_antes, list) else 0}")
print(f"  Métrica 'flux.dlq.messages': {dlq_metric_antes}")
print(f"  Métrica 'flux.dlq.reprocessed': {reproc_metric_antes}")

print("\n" + "=" * 65)
print("2. DISPARANDO POISON PILL (SIMULAÇÃO DE FALHA CRÍTICA)")
print("=" * 65)

key_critica = f"falha-dlq-{int(time.time() * 1000)}"
ev_falha = {
    "type": "pedido.critico",
    "source": "teste-dlq-script",
    "idempotencyKey": key_critica,
    "payload": json.dumps({
        "pedidoId": "PED-POISON-PILL-999",
        "forcar_dlq": True,
        "motivo": "Simulação determinística de erro irrecuperável"
    })
}

st, resp = post("/api/v1/events", ev_falha)
print(f"  [POST /api/v1/events] -> Status: {st} (Ingerido e publicado no tópico principal)")
print(f"  Aguardando retries do Spring Kafka (3 tentativas com backoff)...")

# Aguarda tentativas e desvio para a DLQ
for i in range(1, 6):
    time.sleep(1)
    print(f"    ... aguardando ({i}s/5s)")

print("\n" + "=" * 65)
print("3. VERIFICANDO RECEBIMENTO NA DEAD LETTER QUEUE")
print("=" * 65)

_, dlq_itens_depois = get("/api/v1/dlq")
dlq_metric_depois = get_metric_value("flux.dlq.messages")

print(f"  Mensagens na DLQ após retries: {len(dlq_itens_depois) if isinstance(dlq_itens_depois, list) else 0}")
print(f"  Métrica 'flux.dlq.messages': {dlq_metric_depois} (Variação: +{dlq_metric_depois - dlq_metric_antes})")

item_novo = None
if isinstance(dlq_itens_depois, list) and len(dlq_itens_depois) > 0:
    item_novo = dlq_itens_depois[-1]
    print(f"\n  Detalhes da Mensagem na DLQ:")
    print(f"    - ID DLQ: {item_novo.get('id')}")
    print(f"    - Chave: {item_novo.get('key')}")
    print(f"    - Tópico Original: {item_novo.get('originalTopic')}")
    print(f"    - Motivo do Erro: {item_novo.get('exceptionMessage')}")
    print(f"    - Horário: {item_novo.get('receivedAt')}")

print("\n" + "=" * 65)
print("4. TESTANDO REPROCESSAMENTO A PARTIR DA DLQ (/reprocessar)")
print("=" * 65)

if item_novo:
    id_dlq = item_novo.get("id")
    st_rep, resp_rep = post(f"/api/v1/dlq/{id_dlq}/reprocessar")
    print(f"  [POST /api/v1/dlq/{id_dlq}/reprocessar] -> Status: {st_rep}")
    print(f"  Resposta: {resp_rep}")

    time.sleep(1)
    reproc_metric_depois = get_metric_value("flux.dlq.reprocessed")
    print(f"  Métrica 'flux.dlq.reprocessed': {reproc_metric_depois} (Variação: +{reproc_metric_depois - reproc_metric_antes})")

print("\n" + "=" * 65)
print("5. VERIFICAÇÃO DE SAÚDE OPERACIONAL (/actuator/health)")
print("=" * 65)
h_status, h_data = get("/actuator/health")
print(f"  Health Status: {h_data.get('status')}")
if "components" in h_data and "flux" in h_data["components"]:
    flux_details = h_data["components"]["flux"]["details"]
    print(f"  Mensagens retidas informadas no Health Check: {flux_details.get('mensagensDlqRetidas')}")
