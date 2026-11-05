# Roadmap — Root L Flux

## Marco 1 — Fundação
- [x] Inicialização padronizada do repositório e contexto
- [x] Especificação detalhada de requisitos funcionais e não funcionais em `docs/requirements/requirements.md`
- [x] Especificação da arquitetura hexagonal em `docs/architecture/overview.md`
- [x] Registro de decisões arquiteturais:
  - [x] [ADR-001: Estrutura Inicial do Projeto](../decisions/ADR-001-initial-architecture.md)
  - [x] [ADR-002: Arquitetura Hexagonal e Monólito Modular](../decisions/ADR-002-hexagonal-modular-architecture.md)
  - [x] [ADR-003: Estratégia de Contratos de Eventos, Persistência e Mensageria](../decisions/ADR-003-event-contract-persistence-strategy.md)
- [x] Infraestrutura local com Docker Compose (PostgreSQL 16 e Kafka KRaft)

## Marco 2 — Implementação Core
- [x] Configuração de build Maven 3.9 com Spring Boot 3.4
- [x] Implementação do núcleo de Domínio (`Event`, `EventMetadata`, `FlowExecution`, `ExecutionStatus`)
- [x] Implementação do motor de workflows (`FlowDefinition`, `StepDefinition`, `StepExecution`, `RetryPolicy`)
- [x] Camada de Aplicação (`IngestEventUseCase`, `ManageFlowDefinitionUseCase`, `ExecuteFlowUseCase`, `ReplayExecutionUseCase`)
- [x] Orquestração dirigida a eventos (ingestão dispara automaticamente fluxos vinculados)
- [x] Mecanismo de recuperação e reprocessamento (*Flow Replay*)
- [x] Adaptadores de Persistência JPA (`Event`, `FlowDefinition`, `FlowExecution`, `StepExecution`)
- [x] Adaptadores REST completos (`EventController`, `FlowDefinitionController`, `FlowExecutionController`) com OpenAPI 3.0
- [x] Suíte abrangente de 16 testes automatizados unitários, de integração e contratuais
- [ ] Integração do produtor e consumidor de eventos com Apache Kafka
- [ ] Roteamento para Dead Letter Queue (DLQ) em caso de esgotamento de retries

## Marco 3 — Consolidação
- [ ] Revisão técnica de qualidade e segurança (`technical-review`)
- [ ] Proposta de novos aprendizados técnicos na `knowledge/inbox/`
