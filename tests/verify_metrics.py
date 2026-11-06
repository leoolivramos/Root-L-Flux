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
        return e.code, json.loads(e.read().decode('utf-8'))
    except Exception as e:
        return None, str(e)

def get_prometheus():
    url = f"{BASE_URL}/actuator/prometheus"
    req = urllib.request.Request(url)
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, resp.read().decode('utf-8')
    except Exception as e:
        return None, str(e)

def post(path, body):
    url = f"{BASE_URL}{path}"
    data = json.dumps(body).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json", "Accept": "application/json"})
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, json.loads(resp.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode('utf-8'))
        except Exception:
            return e.code, e.read().decode('utf-8')
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

print("=" * 60)
print("1. VALORES DAS MÉTRICAS ANTES DOS DISPAROS")
print("=" * 60)

metricas = [
    "flux.events.ingested",
    "flux.events.duplicates",
    "flux.workflows.executions",
    "flux.steps.executions"
]

antes = {}
for m in metricas:
    val = get_metric_value(m)
    antes[m] = val
    print(f"  - {m}: {val}")

print("\n" + "=" * 60)
print("2. DISPARANDO REQUISIÇÕES DE NEGÓCIO")
print("=" * 60)

# 2.1 Cadastrar Workflow
flow_payload = {
    "flowId": "fluxo-teste-metricas",
    "name": "Fluxo de Teste de Métricas",
    "triggerEventType": "pedido.metrica",
    "steps": [
        {"stepId": "validacao-dados", "name": "Validação de Dados", "orderIndex": 1, "actionType": "VALIDACAO", "maxAttempts": 2},
        {"stepId": "reserva-estoque", "name": "Reserva de Estoque", "orderIndex": 2, "actionType": "INTEGRACAO", "maxAttempts": 2}
    ]
}
st, resp = post("/api/v1/flows", flow_payload)
print(f"  [POST /api/v1/flows] -> Status: {st}")

# 2.2 Ingerir 3 eventos válidos (devem disparar workflow e incrementar métricas)
for i in range(1, 4):
    ev = {
        "type": "pedido.metrica",
        "source": "teste-metricas-script",
        "idempotencyKey": f"chave-metrica-{int(time.time() * 1000)}-{i}",
        "payload": json.dumps({"item": f"produto-{i}", "quantidade": i})
    }
    st, resp = post("/api/v1/events", ev)
    print(f"  [POST /api/v1/events #{i}] -> Status: {st} | EventId: {resp.get('id') if isinstance(resp, dict) else ''}")

# 2.3 Ingerir 2 eventos duplicados intencionais (para testar métrica de duplicadas)
chave_duplicada = f"chave-colisao-{int(time.time() * 1000)}"
ev_original = {
    "type": "pedido.metrica",
    "source": "teste-metricas-script",
    "idempotencyKey": chave_duplicada,
    "payload": '{"origem": "primeira-chamada"}'
}
st1, _ = post("/api/v1/events", ev_original)
print(f"  [POST /api/v1/events (Primeira vez da chave)] -> Status: {st1} (esperado 201)")

ev_copia = {
    "type": "pedido.metrica",
    "source": "teste-metricas-script",
    "idempotencyKey": chave_duplicada,
    "payload": '{"origem": "segunda-chamada-duplicada"}'
}
st2, r2 = post("/api/v1/events", ev_copia)
print(f"  [POST /api/v1/events (Segunda vez - Colisão)] -> Status: {st2} (esperado 409)")

time.sleep(1) # Aguarda persistência e processamento assíncrono

print("\n" + "=" * 60)
print("3. VALORES DAS MÉTRICAS APÓS OS DISPAROS")
print("=" * 60)

depois = {}
for m in metricas:
    val = get_metric_value(m)
    depois[m] = val
    diff = val - antes[m]
    print(f"  - {m}: {val} (Variação: +{diff})")

print("\n" + "=" * 60)
print("4. AMOSTRA DO SCRAPING PROMETHEUS (/actuator/prometheus)")
print("=" * 60)

p_status, p_text = get_prometheus()
print(f"  Prometheus Status: {p_status}")
if p_status == 200:
    flux_lines = [line for line in p_text.splitlines() if "flux_" in line]
    for line in flux_lines[:15]:
        print(f"    {line}")
    if len(flux_lines) > 15:
        print(f"    ... (+ {len(flux_lines) - 15} linhas de métricas de negócio flux_)")

print("\n" + "=" * 60)
print("5. VERIFICAÇÃO DE SAÚDE OPERACIONAL (/actuator/health)")
print("=" * 60)
h_status, h_data = get("/actuator/health")
print(f"  Health Status: {h_data.get('status')}")
if "components" in h_data and "flux" in h_data["components"]:
    flux_comp = h_data["components"]["flux"]
    print(f"  Componente 'flux': {json.dumps(flux_comp, indent=4, ensure_ascii=False)}")
