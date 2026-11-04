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
- [x] Implementação da camada de Aplicação (`IngestEventUseCase`, `EventIngestionService`, `FlowExecutionOrchestrator`)
- [x] Implementação de Adaptadores de Persistência JPA (`EventPersistenceAdapter`, `FlowExecutionPersistenceAdapter`)
- [x] Implementação de Adaptadores REST (`EventController`, `FlowExecutionController`, `GlobalExceptionHandler`) com OpenAPI 3.0
- [x] Implementação de testes unitários e de integração em `src/test/` e testes estruturais em `tests/`
- [ ] Implementação de integração nativa do produtor e consumidor Apache Kafka
- [ ] Mecanismo avançado de orquestração de etapas encadeadas com tolerância a falhas e DLQ

## Marco 3 — Consolidação
- [ ] Revisão técnica de qualidade e segurança (`technical-review`)
- [ ] Proposta de novos aprendizados técnicos na `knowledge/inbox/`
