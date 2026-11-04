# Requisitos do Sistema — Root L Flux

Este documento especifica os requisitos funcionais e não funcionais do **Root L Flux**, plataforma de orquestração, processamento e gerenciamento de fluxos de eventos distribuídos.

---

## 1. Visão Geral do Sistema

O **Root L Flux** fornece uma infraestrutura desacoplada, resiliente e observável para o processamento de eventos de negócio em pipelines ou fluxos orquestrados. Cada fluxo é composto por etapas (*steps*) que podem executar de forma síncrona ou assíncrona, com garantias de idempotência, auditoria de execução e tolerância a falhas.

---

## 2. Requisitos Funcionais (RF)

### 2.1 Ingestão e Validação de Eventos

- **RF-01 (Ingestão via API REST):** O sistema deve expor endpoints REST (`POST /api/v1/events`) para receber eventos externos em formato JSON, gerando identificador único (`eventId`), timestamp de recebimento e rastreador de correlação (`correlationId`).
- **RF-02 (Validação de Contratos de Eventos):** O sistema deve validar a estrutura, schema e metadados obrigatórios do evento antes de aceitá-lo para processamento. Eventos inválidos devem ser rejeitados imediatamente com código HTTP 400 e payload detalhando as violações.
- **RF-03 (Idempotência e Deduplicação):** O sistema deve suportar uma chave de idempotência (`idempotencyKey` ou `eventId`). Eventos duplicados dentro de uma janela configurável não devem ser reexecutados, retornando o resultado ou estado da execução anterior com status HTTP 200/409 apropriado.

### 2.2 Gerenciamento de Fluxos (Workflows)

- **RF-04 (Definição de Fluxos):** O sistema deve permitir a definição de fluxos de eventos (`FlowDefinition`), especificando:
  - Identificador único do fluxo (`flowId` / `flowKey`) e versão (`version`).
  - Evento de gatilho (`triggerEventType`).
  - Sequência de etapas (`steps`), dependências entre etapas e condições de execução.
  - Políticas de retry e tempo limite (*timeout*).
- **RF-05 (Execução e Transição de Estados):** O motor de execução deve gerenciar o ciclo de vida da execução de um fluxo (`FlowExecution`):
  - Estados: `RECEIVED`, `RUNNING`, `COMPLETED`, `FAILED`, `RETRYING`, `CANCELLED`.
  - Transições atômicas e auditáveis gravadas com histórico temporal.

### 2.3 Roteamento e Processamento

- **RF-06 (Roteamento de Eventos):** O motor de roteamento deve correlacionar eventos de entrada às definições de fluxo ativas correspondentes e despachar as etapas para os processadores adequados.
- **RF-07 (Execução de Etapas - Steps):** Cada etapa deve receber o payload do evento ou a saída de etapas anteriores, executar a ação pretendida (transformação, chamada HTTP, validação ou publicação de evento secundário) e persistir o resultado (`StepExecution`).

### 2.4 Resiliência, Retries e DLQ

- **RF-08 (Política de Retry com Backoff):** Em caso de falha transitória em uma etapa, o sistema deve executar novas tentativas com política configurável (ex.: backoff exponencial com jitter e número máximo de tentativas).
- **RF-09 (Dead Letter Queue - DLQ):** Eventos ou etapas que excederem as tentativas máximas de retry devem ser roteados para uma fila de eventos mortos (DLQ) com contexto detalhado da causa raiz da falha.
- **RF-10 (Reprocessamento / Flow Replay):** O sistema deve permitir solicitar o reprocessamento manual ou agendado de um fluxo ou evento com falha (`POST /api/v1/executions/{id}/replay`).

---

## 3. Requisitos Não Funcionais (RNF)

- **RNF-01 (Arquitetura Hexagonal & Baixo Acoplamento):** O domínio deve ser estritamente isolado de frameworks, bibliotecas de serialização e detalhes de infraestrutura (Ports & Adapters).
- **RNF-02 (Desempenho & Baixa Latência):** O tempo de resposta para aceitação e enfileiramento de um evento na API REST deve ser inferior a 50ms no percentil 95 (P95) em ambiente local.
- **RNF-03 (Observabilidade Completa):** Todos os eventos e fluxos devem propagar contexto de rastreabilidade distribuída (`traceparent` W3C / OpenTelemetry) e gerar logs estruturados em JSON com `traceId`, `spanId` e `flowExecutionId`.
- **RNF-04 (Portabilidade e Local-First):** Toda a stack deve ser 100% executável localmente via Docker Compose (PostgreSQL, Kafka) e scripts portáteis para Windows e Linux.
- **RNF-05 (Segurança & Higiene de Segredos):** Nenhuma credencial ou token sensível deve ser mantido em código-fonte ou controle de versão, sendo injetados via variáveis de ambiente.
- **RNF-06 (Concorrência Segura & Idempotência):** Operações concorrentes de atualização de estado de fluxo devem utilizar optimistic locking (via versão de registro) para evitar condições de corrida (*race conditions*).
- **RNF-07 (Testabilidade & Cobertura):** Módulos de domínio e aplicação devem ter 100% de cobertura por testes unitários sem dependência de containers ou banco de dados externo; adaptadores devem ser testados com mocks e Testcontainers/H2.
