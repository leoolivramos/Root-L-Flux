# Project Context — Root L Flux

- **Identificador (Slug):** `root-l-flux`
- **Categoria:** `personal`
- **Data de Criação:** `2026-11-04`
- **Estado Atual:** Em Desenvolvimento — Marco 1 (Fundação Concluída) / Marco 2 (Implementação Core)

---

## 1. Objetivo

Desenvolver uma plataforma de orquestração, processamento e gerenciamento de fluxos de eventos distribuídos, utilizando Java e Spring Boot.

O Root L Flux tem como objetivo fornecer uma infraestrutura extensível para ingestão, validação, roteamento, processamento e rastreamento de eventos, permitindo a definição e execução de fluxos compostos por etapas independentes e interdependentes.

A plataforma deverá oferecer mecanismos de confiabilidade, recuperação de falhas, idempotência, observabilidade e auditoria, mantendo consistência operacional em ambientes distribuídos.

## 2. Problema

Sistemas distribuídos frequentemente dependem de integrações complexas entre serviços, aplicações e fontes de dados. O processamento assíncrono de eventos introduz desafios relacionados à coordenação de operações, consistência eventual, duplicidade de mensagens, falhas parciais, rastreabilidade e recuperação de execuções.

Soluções tradicionais de integração nem sempre oferecem visibilidade suficiente sobre o ciclo de vida dos eventos e a execução dos processos que dependem deles.

O Root L Flux resolve esse problema por meio de uma plataforma centralizada de gerenciamento de fluxos, oferecendo:

- Padronização da ingestão e distribuição de eventos.
- Orquestração de processos assíncronos e dependências entre etapas.
- Controle do ciclo de vida e estado das execuções.
- Garantias operacionais de idempotência e mecanismos de recuperação.
- Rastreamento ponta a ponta de eventos e operações.
- Identificação, isolamento e tratamento de falhas.
- Capacidade de reprocessamento controlado e auditável.

## 3. Escopo

### Dentro do Escopo

- API REST para gerenciamento de fluxos e ingestão de eventos.
- Motor de processamento e roteamento de eventos.
- Motor de orquestração de workflows.
- Definição de etapas, dependências e condições de execução.
- Integração com Apache Kafka para mensageria.
- Persistência de metadados e estados de execução.
- Controle de idempotência e deduplicação.
- Políticas configuráveis de retry e Dead Letter Queue (DLQ).
- Reprocessamento e recuperação de execuções (Flow Replay).
- Versionamento e validação de contratos de eventos.
- Autenticação, autorização e auditoria.
- Observabilidade com métricas, logs estruturados e distributed tracing.
- Console para visualização e gerenciamento operacional.
- Testes unitários, de integração e de resiliência.
- Empacotamento e execução por Docker Compose.

### Fora do Escopo

- Substituição de plataformas completas de mensageria, como Apache Kafka.
- Desenvolvimento de um banco de dados próprio.
- Implementação de um mecanismo genérico de ETL ou substituição de plataformas como Apache Airflow.
- Processamento de grandes volumes de dados analíticos em formato batch como finalidade principal.
- Inteligência artificial ou decisões baseadas em modelos de linguagem.
- Interface de desenvolvimento visual avançada de workflows nesta primeira fase.
- Suporte inicial a múltiplos provedores de nuvem e infraestrutura gerenciada.
- Arquitetura baseada obrigatoriamente em microsserviços desde a primeira versão.

## 4. Tecnologias

- **Linguagem Principal:** Java 21+ / 25+ / 27
- **Framework Backend:** Spring Boot 3.4.2
- **Documentação de API:** OpenAPI 3.0 / SpringDoc UI
- **Persistência / Dados:** PostgreSQL 16 (H2 em memória para testes) / Spring Data JPA
- **Mensageria:** Apache Kafka 3.8 (KRaft mode)
- **Testes:** JUnit 5 / Mockito / Spring Boot Test
- **Containerização:** Docker / Docker Compose
- **Controle de Versão:** Git (repositório independente)

## 5. Arquitetura Atual

A arquitetura consolidada segue o padrão **Monólito Modular com Arquitetura Hexagonal (Ports and Adapters)**:

- **Núcleo de Domínio (`domain`):** Entidades (`Event`, `FlowExecution`), Value Objects (`EventId`, `EventMetadata`, `ExecutionStatus`) e regras de negócio puras (sem dependência de Spring ou bibliotecas de terceiros).
- **Camada de Aplicação (`application`):** Portas de entrada (`IngestEventUseCase`, `ExecuteFlowUseCase`), portas de saída (`EventRepositoryPort`, `FlowExecutionRepositoryPort`, `EventPublisherPort`) e serviços orquestradores (`EventIngestionService`, `FlowExecutionOrchestrator`).
- **Adaptadores de Infraestrutura (`infrastructure`):**
  - *Inbound:* Controladores REST (`EventController`, `FlowExecutionController`) e tratamento global de erros (`GlobalExceptionHandler`).
  - *Outbound:* Adaptadores JPA para persistência em banco relacional e adaptadores de mensageria para publicação de eventos.

Consulte [`docs/architecture/overview.md`](./docs/architecture/overview.md) e os ADRs em [`docs/decisions/`](./docs/decisions/).

## 6. Restrições

- Conformidade com as políticas globais de segurança (`core/policies/security.md`) e permissões (`core/policies/permissions.md`).
- Proibido armazenar segredos ou credenciais em arquivos versionados.
- Execução Local-first.
- Preferência por tecnologias open source e infraestrutura reproduzível localmente.
- Regras de negócio independentes de frameworks e mecanismos de infraestrutura.
- Garantias de idempotência e rastreabilidade nas operações críticas.
- Falhas de processamento devem possuir comportamento explícito e observável.
- Evitar complexidade arquitetural sem justificativa técnica documentada.
- Decisões arquiteturais relevantes registradas por meio de ADRs.

## 7. Próximas Entregas

- [x] Detalhar requisitos funcionais e não funcionais em `docs/requirements/requirements.md`.
- [x] Consolidar a arquitetura inicial em `docs/architecture/overview.md`.
- [x] Elaborar ADR-001: Estrutura Inicial do Projeto.
- [x] Elaborar ADR-002: Arquitetura Hexagonal e Monólito Modular.
- [x] Elaborar ADR-003: Estratégia de Contratos de Eventos, Persistência e Mensageria.
- [x] Estabelecer estrutura Maven e organização dos pacotes Java.
- [x] Configurar ambiente local com Docker Compose (PostgreSQL 16 e Kafka KRaft).
- [x] Implementar primeiro incremento funcional em `src/` (Ingestão, validação e persistência com idempotência).
- [x] Implementar testes unitários, testes de integração e testes de contrato em `tests/`.
- [x] Implementar motor de workflows com etapas sequenciais, políticas de retry e reprocessamento (*Flow Replay*).
- [x] Implementar orquestração dirigida a eventos com disparo automático ao ingerir eventos associados.
- [x] Demonstrar publicação e consumo ponta a ponta integrando Apache Kafka localmente.
- [x] Roteamento para Dead Letter Queue (DLQ) em caso de esgotamento de retries.
- [x] Elaborar ADR-004: Observabilidade com Métricas Prometheus, Distributed Tracing e Logs Estruturados.
- [x] Implementar observabilidade ponta a ponta (Micrometer, Prometheus, OpenTelemetry Tracing, MDC, Health Checks customizados e painel web).

## 8. Referências Relevantes

- Knowledge Base Central: `../../knowledge/index.md`
- Referências do Projeto: [`docs/references/references.md`](./docs/references/references.md)
- Decisões Arquiteturais: [`docs/decisions/`](./docs/decisions/)
- Documentação oficial: [Spring Boot](https://docs.spring.io/spring-boot/index.html)
- Documentação oficial: [Apache Kafka](https://kafka.apache.org/documentation/)

---

**Identidade do Projeto:** Root L Flux

**Direção técnica:** Java + Spring Boot | Arquitetura Hexagonal | Event-Driven Architecture | Resiliência | Observabilidade

**Princípio de desenvolvimento:** Construir uma plataforma confiável, extensível e tecnicamente justificável, priorizando simplicidade arquitetural, consistência e qualidade de engenharia.
