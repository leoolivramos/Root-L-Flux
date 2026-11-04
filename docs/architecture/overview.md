# Visão Geral da Arquitetura — Root L Flux

## 1. Visão Geral e Princípios Arquiteturais

O **Root L Flux** adota a **Arquitetura Hexagonal (Ports and Adapters)** implementada como um **Monólito Modular** em Java e Spring Boot.

Os princípios centrais da arquitetura são:

1. **Independência do Domínio:** As regras centrais de negócio (eventos, fluxos, etapas, estados e idempotência) residem no pacote `domain` e não possuem dependências de frameworks (como Spring), bancos de dados ou bibliotecas de serialização.
2. **Inversão de Dependências (Ports and Adapters):** O núcleo da aplicação comunica-se com o mundo externo através de portas explícitas (*Input Ports* para casos de uso e *Output Ports* para persistência, mensageria e serviços externos).
3. **Comunicação Orientada a Eventos (EDA):** Ingestão desacoplada, processamento reativo e rastreabilidade ponta a ponta com Apache Kafka.
4. **Idempotência por Design:** Toda operação de transição de estado e ingestão de evento é protegida contra duplicidade por identificadores determinísticos e travas otimistas.
5. **Observabilidade Nativa:** Propagação de contexto distribuído (`traceId`, `correlationId`, `spanId`) em todas as camadas e mensagens.

---

## 2. Diagrama de Camadas e Componentes

```text
               +---------------------------------------------------+
               |                 Adaptadores de Entrada            |
               |   [ REST Controllers ]      [ Kafka Consumers ]   |
               +-------------------------+-------------------------+
                                         |
                                         v
               +---------------------------------------------------+
               |               Camada de Aplicação                 |
               |                  (Input Ports)                    |
               |   - IngestEventUseCase                            |
               |   - ExecuteFlowUseCase                            |
               |   - ReplayExecutionUseCase                        |
               |   - QueryExecutionUseCase                         |
               +-------------------------+-------------------------+
                                         |
                                         v
               +---------------------------------------------------+
               |               Núcleo de Domínio                   |
               |   - Entidades: Event, FlowDefinition, FlowExecution|
               |   - Value Objects: ExecutionStatus, RetryPolicy   |
               |   - Regras: Idempotência, Validação, Transições   |
               +-------------------------+-------------------------+
                                         |
                                         v
               +---------------------------------------------------+
               |               Portas de Saída                     |
               |                  (Output Ports)                   |
               |   - EventRepositoryPort                           |
               |   - FlowExecutionRepositoryPort                   |
               |   - EventPublisherPort                            |
               +-------------------------+-------------------------+
                                         |
                                         v
               +---------------------------------------------------+
               |                 Adaptadores de Saída              |
               |   [ JPA / PostgreSQL ]       [ Kafka Producer ]   |
               +---------------------------------------------------+
```

---

## 3. Estrutura de Pacotes (`src/main/java/com/rootl/flux/`)

```text
com.rootl.flux
├── RootLFluxApplication.java
├── domain/
│   ├── model/
│   │   ├── Event.java
│   │   ├── EventId.java
│   │   ├── EventMetadata.java
│   │   ├── FlowDefinition.java
│   │   ├── StepDefinition.java
│   │   ├── FlowExecution.java
│   │   ├── StepExecution.java
│   │   ├── ExecutionStatus.java
│   │   └── RetryPolicy.java
│   └── exception/
│       ├── EventValidationException.java
│       ├── DuplicateEventException.java
│       └── FlowNotFoundException.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── IngestEventUseCase.java
│   │   │   ├── ExecuteFlowUseCase.java
│   │   │   ├── QueryFlowExecutionUseCase.java
│   │   │   └── ReplayExecutionUseCase.java
│   │   └── out/
│   │       ├── EventRepositoryPort.java
│   │       ├── FlowDefinitionRepositoryPort.java
│   │       ├── FlowExecutionRepositoryPort.java
│   │       └── EventPublisherPort.java
│   └── service/
│       ├── EventIngestionService.java
│       └── FlowExecutionOrchestrator.java
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── EventController.java
    │   │       ├── FlowExecutionController.java
    │   │       ├── dto/
    │   │       └── GlobalExceptionHandler.java
    │   └── out/
    │       ├── persistence/
    │       │   ├── entity/
    │       │   ├── repository/
    │       │   └── adapter/
    │       └── messaging/
    │           └── kafka/
    └── config/
        ├── BeanConfig.java
        └── OpenApiConfig.java
```

---

## 4. Fluxo de Execução Típico

1. **Ingestão:** Requisição `POST /api/v1/events` recebida pelo `EventController`.
2. **Validação:** DTO validado e repassado para `IngestEventUseCase`.
3. **Idempotência:** O serviço consulta `EventRepositoryPort` para verificar se `idempotencyKey` ou `eventId` já foram processados.
4. **Persistência do Evento:** O evento é salvo como recebido (`RECEIVED`).
5. **Roteamento / Publicação:** O evento é publicado no broker (Kafka) via `EventPublisherPort` ou despachado para os fluxos cadastrados.
6. **Execução do Fluxo:** O `FlowExecutionOrchestrator` cria uma `FlowExecution`, aciona cada `StepDefinition` respeitando precedências e retries, gravando os estados atualizados.
