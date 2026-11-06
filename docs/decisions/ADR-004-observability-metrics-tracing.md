# ADR-004: Observabilidade com Métricas Prometheus, Distributed Tracing e Logs Estruturados

- **Estado:** Aprovado
- **Data:** 2026-11-06
- **Projeto:** `root-l-flux`
- **Autor:** Root L Flux Team

---

## 1. Contexto

A plataforma **Root L Flux** opera como um motor de processamento assíncrono e orquestração de eventos distribuídos, integrando APIs HTTP, persistência relacional em PostgreSQL e mensageria de alta vazão no Apache Kafka.

Em arquiteturas orientadas a eventos e fluxos assíncronos, o rastreamento ponta a ponta e a monitoração da saúde operacional em tempo real são indispensáveis para diagnosticar latências em cauda (*tail latency*), falhas parciais e desvios para Dead Letter Queue (DLQ).

## 2. Problema

Definir e padronizar os três pilares da observabilidade no projeto:
1. **Métricas operacionais e de negócio:** Coleta, agregação e exportação padronizada de contadores, timers e distribuições.
2. **Distributed Tracing (Rastreamento Distribuído):** Propagação de contexto entre requisições HTTP, eventos no Kafka e etapas de workflows.
3. **Logs Estruturados e Correlacionados:** Associação imediata de cada linha de log com o `traceId`, `spanId` e `correlationId` via MDC.
4. **Health Checks Operacionais:** Verificação da integridade do sistema exposta via Spring Boot Actuator.

## 3. Decisão

1. **Métricas com Micrometer e Prometheus:**
   - Adotar o **Micrometer Core** integrado nativamente ao **Spring Boot Actuator**.
   - Incluir a dependência `io.micrometer:micrometer-registry-prometheus` para expor o endpoint de scraping em `/actuator/prometheus`.
   - Implementar a porta `MetricsPort` e o adaptador `MicrometerMetricsAdapter` para emitir métricas customizadas de negócio:
     - `flux.events.ingested` (tipo, origem, status)
     - `flux.events.duplicates` (tipo, origem)
     - `flux.workflows.executions` e `flux.workflows.duration` (flowId, status)
     - `flux.steps.executions` e `flux.steps.duration` (actionType, status)
     - `flux.dlq.messages` (motivo) e `flux.dlq.reprocessed` (status)

2. **Distributed Tracing com OpenTelemetry e W3C Trace Context:**
   - Adotar **Micrometer Tracing** com a ponte OpenTelemetry (`io.micrometer:micrometer-tracing-bridge-otel`).
   - Propagação via especificação oficial **W3C Trace Context** (`traceparent` header).
   - Habilitação da instrumentação automática no Spring Kafka (`observation-enabled: true`) no `KafkaTemplate` e nos `@KafkaListener`.

3. **Correlação e Logs Estruturados:**
   - Implementar o filtro HTTP de alta prioridade `CorrelationLoggingFilter`.
   - Injeção automática de `correlationId`, `traceId` e `spanId` no **Mapped Diagnostic Context (MDC)** do SLF4J/Logback.
   - Retorno automático dos cabeçalhos HTTP `X-Correlation-Id` e `X-Trace-Id` em todas as respostas da API REST.
   - Padrão de log configurado: `"%5p [${spring.application.name:root-l-flux},%X{traceId:-},%X{spanId:-}]"`.

4. **Health Check Customizado:**
   - Implementar o componente `FluxHealthIndicator` exposto em `/actuator/health`.
   - Monitoramento contínuo de status da plataforma, contagem de fluxos cadastrados e mensagens retidas na Dead Letter Queue.

5. **Console Web de Orquestração:**
   - Adição da aba de **Observabilidade** no console web (`/`), exibindo status de integridade, estatísticas da JVM em tempo real e links diretos para `/actuator/prometheus` e `/actuator/health`.

## 4. Consequências

- **Positivas:**
  - Visibilidade total do ciclo de vida dos eventos desde a recepção HTTP até a entrega nos tópicos Kafka e execução de workflows.
  - Correlação trivial de logs com um único `traceId` no Grafana Loki / ElasticSearch.
  - Prontidão para integração direta com dashboards Prometheus e Grafana.
  - Isolamento respeitado: o núcleo de domínio permanece 100% puro, interagindo com métricas através da porta hexagonal `MetricsPort`.

- **Negativas / Mitigações:**
  - Pequeno overhead adicional de CPU para geração de identificadores de trace e headers (desprezível em comparação à latência de rede/disco).
  - Em ambientes de altíssima volumetria de produção, a taxa de amostragem (`management.tracing.sampling.probability`) deve ser calibrada (e.g. 5% a 10%), mantendo 100% em desenvolvimento e homologação.
