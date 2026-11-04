# ADR-003: Estratégia de Contratos de Eventos, Persistência e Mensageria

- **Estado:** Proposto
- **Data:** 2026-11-04
- **Projeto:** `root-l-flux`
- **Autor:** Root L Flux Team

---

## 1. Contexto

A plataforma **Root L Flux** processa eventos distribuídos que exigem garantia de idempotência, histórico de auditoria e interoperabilidade entre sistemas externos.

## 2. Problema

Definir:
1. O formato e os padrões de metadados para os eventos ingeridos e manipulados.
2. A tecnologia e a modelagem relacional de persistência para eventos e execuções de fluxo.
3. A ferramenta de mensageria assíncrona para distribuição e reprocessamento de eventos.

## 3. Decisão

1. **Padrão de Contrato de Eventos:**
   - Adotar especificação inspirada no padrão [CloudEvents v1.0](https://cloudevents.io/) com envelope JSON unificado:
     - `id`: UUID único do evento.
     - `source`: Origem geradora do evento (URI / nome do serviço).
     - `type`: Tipo do evento em notação pontuada (`e.g. order.created`, `payment.confirmed`).
     - `timestamp`: Momento UTC em ISO-8601.
     - `idempotencyKey`: Chave opcional de desduplicação fornecida pelo cliente (ou fallback para `id`).
     - `correlationId`: Identificador para agrupamento de eventos e transações relacionadas.
     - `traceContext`: Metadados W3C Trace Context (`traceparent`).
     - `data`: Objeto JSON com os dados do payload de negócio.

2. **Estratégia de Persistência:**
   - Adotar **PostgreSQL 16+** como banco relacional central, com migrações gerenciadas pelo **Flyway**.
   - Modelagem de tabelas essenciais:
     - `flux_events`: Eventos ingeridos, metadados, payload JSONB e hash/chave de idempotência.
     - `flux_flow_definitions`: Definições cadastradas de fluxos (gatilhos, etapas, retries).
     - `flux_flow_executions`: Execuções com estado (`status`), timestamps e versão (optimistic lock).
     - `flux_step_executions`: Execuções de cada etapa individual dentro de um fluxo.
   - Ambientes de teste: repositórios em memória ou PostgreSQL via Testcontainers.

3. **Mensageria com Apache Kafka:**
   - Adotar **Apache Kafka** (em modo KRaft para execução local limpa sem dependência de ZooKeeper) via Docker Compose.
   - Tópicos primários:
     - `root-l-flux.events.raw`: Ingestão bruta de eventos.
     - `root-l-flux.events.dlq`: Dead Letter Queue para eventos que excederam retries.

## 4. Consequências

- Rastreabilidade ponta a ponta e auditoria completa em PostgreSQL.
- Baixo overhead operacional local via Docker Compose (PostgreSQL + Kafka KRaft).
- Compatibilidade com ferramentas e especificações abertas da indústria.
