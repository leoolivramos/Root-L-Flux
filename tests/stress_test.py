#!/usr/bin/env python3
"""Utilitário de Teste de Carga e Estresse do Root L Flux.

Executa requisições concorrentes de alta volumetria contra a API REST e o motor de eventos,
avaliando throughput (req/s), latência (percentis P50, P95, P99) e consistência sob estresse.
"""

from __future__ import annotations

import argparse
import json
import statistics
import sys
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed


class StressTestResult:
    def __init__(self):
        self.total_requests = 0
        self.successful_requests = 0
        self.conflict_requests = 0  # 409 (Idempotência esperada)
        self.failed_requests = 0
        self.latencies_ms: list[float] = []
        self.status_codes: dict[int, int] = {}
        self.start_time = 0.0
        self.end_time = 0.0

    @property
    def total_time(self) -> float:
        return max(0.001, self.end_time - self.start_time)

    @property
    def rps(self) -> float:
        return self.total_requests / self.total_time

    def add_response(self, status: int, latency_ms: float):
        self.total_requests += 1
        self.latencies_ms.append(latency_ms)
        self.status_codes[status] = self.status_codes.get(status, 0) + 1

        if status in (200, 201, 204):
            self.successful_requests += 1
        elif status == 409:
            self.conflict_requests += 1
        else:
            self.failed_requests += 1


def make_http_request(url: str, method: str, data: dict | None = None) -> tuple[int, float]:
    headers = {"Content-Type": "application/json"}
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)

    t0 = time.perf_counter()
    status = 500
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            status = resp.status
    except urllib.error.HTTPError as e:
        status = e.code
    except Exception:
        status = 0
    t1 = time.perf_counter()

    latency_ms = (t1 - t0) * 1000.0
    return status, latency_ms


def run_stress_test(
    base_url: str,
    total_requests: int,
    concurrency: int,
    mode: str,
) -> StressTestResult:
    results = StressTestResult()
    print("=" * 65)
    print(" 🔥 INICIANDO TESTE DE CARGA PESADO — ROOT L FLUX 🔥")
    print(f" URL Base       : {base_url}")
    print(f" Modo de Teste  : {mode.upper()}")
    print(f" Total de Req   : {total_requests}")
    print(f" Concorrência   : {concurrency} threads simultâneas")
    print("=" * 65)

    events_endpoint = f"{base_url.rstrip('/')}/api/v1/events"

    # Preparar gerador de payloads conforme o modo
    def generate_payload(idx: int) -> dict:
        if mode == "idempotency":
            # Estresse extremo de concorrência com colisões intencionais de chave
            batch_key = f"chave-colisao-{idx % 10}"
            return {
                "type": "pedido.concorrente",
                "source": "teste-estresse",
                "payload": json.dumps({"reqId": idx, "thread": "worker"}),
                "idempotencyKey": batch_key,
                "correlationId": f"corr-{idx}",
            }
        elif mode == "dlq":
            # Estresse de envio para DLQ (simulação de poison pills)
            return {
                "type": "pedido.critico",
                "source": "teste-carga-dlq",
                "payload": json.dumps({"reqId": idx, "forcar_dlq": True}),
                "idempotencyKey": f"dlq-key-{idx}-{time.time_ns()}",
                "correlationId": f"corr-dlq-{idx}",
            }
        else:  # Modo 'ingest' padrão (alta volumetria de eventos únicos)
            return {
                "type": "pedido.carga",
                "source": "teste-carga-massiva",
                "payload": json.dumps({"pedidoId": f"PED-{idx}", "valor": 99.90 + (idx % 100)}),
                "idempotencyKey": f"key-massiva-{idx}-{time.time_ns()}",
                "correlationId": f"corr-{idx}",
            }

    results.start_time = time.perf_counter()

    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [
            executor.submit(make_http_request, events_endpoint, "POST", generate_payload(i))
            for i in range(total_requests)
        ]

        completed = 0
        for future in as_completed(futures):
            status, latency = future.result()
            results.add_response(status, latency)
            completed += 1
            if completed % max(1, total_requests // 10) == 0 or completed == total_requests:
                prog = (completed / total_requests) * 100
                print(f"  ⚡ Progresso: {completed}/{total_requests} ({prog:5.1f}%) | "
                      f"Última latência: {latency:6.2f}ms | Status: {status}")

    results.end_time = time.perf_counter()
    return results


def print_summary(res: StressTestResult):
    print("\n" + "=" * 65)
    print(" 📊 RELATÓRIO CONSOLIDADO DO TESTE DE ESTRESSE")
    print("=" * 65)
    print(f" Tempo Total Decorrido : {res.total_time:.3f} s")
    print(f" Total de Requisições  : {res.total_requests}")
    print(f" Taxa de Vazão (RPS)   : {res.rps:.2f} req/s")
    print("-" * 65)
    print(f" Requisições com Sucesso (2xx)    : {res.successful_requests}")
    print(f" Conflitos de Idempotência (409) : {res.conflict_requests}")
    print(f" Requisições com Falha / Erro     : {res.failed_requests}")
    print("-" * 65)
    print(" Distribuição de Códigos HTTP:")
    for code, count in sorted(res.status_codes.items()):
        pct = (count / res.total_requests) * 100
        print(f"   HTTP {code}: {count:6d} ({pct:5.1f}%)")

    if res.latencies_ms:
        lats = sorted(res.latencies_ms)
        n = len(lats)
        p50 = lats[int(n * 0.50)]
        p90 = lats[int(n * 0.90)]
        p95 = lats[int(n * 0.95)]
        p99 = lats[int(n * 0.99)]
        avg = statistics.mean(lats)

        print("-" * 65)
        print(" Distribuição de Latência (ms):")
        print(f"   Mínima : {lats[0]:6.2f} ms")
        print(f"   Média  : {avg:6.2f} ms")
        print(f"   P50    : {p50:6.2f} ms (mediana)")
        print(f"   P90    : {p90:6.2f} ms")
        print(f"   P95    : {p95:6.2f} ms")
        print(f"   P99    : {p99:6.2f} ms")
        print(f"   Máxima : {lats[-1]:6.2f} ms")
    print("=" * 65)


def main():
    parser = argparse.ArgumentParser(description="Teste de Carga Pesado para Root L Flux")
    parser.add_argument("--url", default="http://localhost:8080", help="URL base da API (padrão: http://localhost:8080)")
    parser.add_argument("-n", "--requests", type=int, default=1000, help="Número total de requisições (padrão: 1000)")
    parser.add_argument("-c", "--concurrency", type=int, default=50, help="Concorrência simultânea de threads (padrão: 50)")
    parser.add_argument("--mode", choices=["ingest", "idempotency", "dlq"], default="ingest",
                        help="Modo de estresse: ingest (alta vazão única), idempotency (colisão massiva), dlq (falhas para DLQ)")

    args = parser.parse_args()

    # Checar se a aplicação está acessível
    try:
        with urllib.request.urlopen(f"{args.url}/api-docs", timeout=3):
            pass
    except Exception as e:
        print(f"⚠️ AVISO: Não foi possível conectar a {args.url}. Certifique-se de que a aplicação está rodando!")
        print(f"Detalhe: {e}")
        sys.exit(1)

    result = run_stress_test(args.url, args.requests, args.concurrency, args.mode)
    print_summary(result)


if __name__ == "__main__":
    main()
