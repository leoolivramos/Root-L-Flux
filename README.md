# Root L Flux

> Orquestrador leve e desacoplado para processamento de fluxos de eventos distribuídos, construído com Java 21+ e Spring Boot 3.4.

O **Root L Flux** foi feito para resolver um problema clássico de quem lida com microsserviços e mensageria: como receber eventos de vários sistemas, garantir que nenhum evento repetido faça besteira (idempotência), rodar as etapas do fluxo na ordem certa, recuperar falhas com Dead Letter Queue (DLQ) e saber exatamente o estado de cada execução sem dor de cabeça.

---

## 🖥️ Console Web de Orquestração

Para facilitar o dia a dia e não depender de Postman ou comandos manuais, o projeto já vem com uma **interface web integrada**:

👉 **Acesse direto no navegador:** `http://localhost:8080/` (ou `/ui`)

### O que você faz pelo Console Web:
1. **Disparo Rápido de Eventos:** Envie eventos de teste com 1 clique usando templates prontos (ex: *Pedido Normal* ou *Simular Falha DLQ*).
2. **Visualização de Workflows:** Veja os fluxos cadastrados com suas etapas em pipeline (ex: `Validação de Estoque` ➔ `Processar Pagamento` ➔ `Emitir Nota Fiscal`).
3. **Acompanhamento de Execuções:** Veja o histórico e as etapas de cada fluxo. Se algo deu errado, use o botão **Reexecutar (Flow Replay)** para tentar de novo sem perder o contexto.
4. **Central da Dead Letter Queue (DLQ):** Veja na hora mensagens que falharam após todas as tentativas no Kafka, com o motivo do erro e o botão **Reenviar** para jogar a mensagem de volta na fila principal.

---

## O que ele resolve na prática

- **Ingestão com validação real:** Se o payload ou metadados essenciais estiverem errados, o sistema rejeita logo na porta de entrada com código e mensagem clara.
- **Idempotência nativa:** Chegou evento repetido com a mesma chave? Ele não reprocessa e devolve status de conflito amigável.
- **Domínio puro (Arquitetura Hexagonal):** As regras do negócio não dependem do Spring, do banco ou do broker. Se amanhã trocar o banco ou mensageria, a lógica continua intacta.
- **Apache Kafka + DLQ Integrados:** Publicador assíncrono, consumidor com retries configuráveis e desvio automático para a Dead Letter Queue quando as tentativas esgotam.
- **Pronto pro dia a dia:** Roda local com banco em memória em 5 segundos, mas já vem configurado com Docker Compose para subir PostgreSQL e Apache Kafka (KRaft) quando precisar de carga real.
- **Swagger & Console prontos:** Subiu a aplicação, a interface web e a documentação interativa já estão no ar.

---

## Tecnologias

- **Java 21+** (compatível com Java 25 e 27)
- **Spring Boot 3.4** (Web, Data JPA, Validation, Actuator, Kafka)
- **Apache Kafka 3.8** (com tópicos de eventos e Dead Letter Queue em KRaft)
- **PostgreSQL 16** / H2 (desenvolvimento ágil e testes rápidos)
- **Console Web Integrado** (HTML5, CSS moderno e Vanilla JS, zero dependências externas)
- **OpenAPI 3 / Swagger UI**
- **JUnit 5 & Mockito** (20 testes automatizados cobrindo domínio, REST e Kafka)

---

## Como rodar localmente

### 1. Pré-requisitos
- JDK 21 ou superior instalado.
- Docker (opcional, caso queira rodar o PostgreSQL e o Kafka locais em containers).

### 2. Rodando a aplicação
Você não precisa nem instalar o Maven na máquina, basta usar o wrapper já incluído:

```bash
# No Windows (PowerShell):
.\mvnw.cmd spring-boot:run

# No Linux / macOS:
./mvnw spring-boot:run
```

- **Console Web de Orquestração:** `http://localhost:8080/`
- **Documentação da API (Swagger):** `http://localhost:8080/swagger-ui.html`

### 3. Rodando a suíte de testes
```bash
.\mvnw.cmd test
```

### 4. Subindo infraestrutura completa (PostgreSQL + Kafka)
Se quiser rodar o ambiente completo com os containers oficiais:

```bash
docker compose up -d
```

---

## Exemplos de uso via API

### 1. Ingerir um evento (dispara workflows vinculados automaticamente)
```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "type": "pedido.criado",
    "source": "checkout-service",
    "payload": "{\"pedidoId\":\"1054\",\"valor\":199.90}",
    "idempotencyKey": "chave-unica-pedido-1054",
    "correlationId": "corr-xyz-789"
  }'
```

### 2. Cadastrar um novo workflow
```bash
curl -X POST http://localhost:8080/api/v1/flows \
  -H "Content-Type: application/json" \
  -d '{
    "flowId": "fluxo-pedidos",
    "name": "Processamento de Pedidos",
    "triggerEventType": "pedido.criado",
    "steps": [
      { "stepId": "validar-estoque", "name": "Validar Estoque", "orderIndex": 1, "actionType": "VALIDACAO", "maxAttempts": 3 },
      { "stepId": "processar-pagamento", "name": "Processar Pagamento", "orderIndex": 2, "actionType": "INTEGRACAO", "maxAttempts": 3 }
    ]
  }'
```

### 3. Inspecionar e reprocessar mensagens da DLQ
```bash
# Listar mensagens na Dead Letter Queue:
curl http://localhost:8080/api/v1/dlq

# Reenviar mensagem com falha de volta para a fila principal:
curl -X POST http://localhost:8080/api/v1/dlq/<ID_DA_MENSAGEM>/reprocessar
```

---

## Observabilidade (Métricas, Logs e Tracing)

O **Root L Flux** possui uma camada completa de observabilidade para monitorar a saúde e o ciclo de vida dos eventos em tempo real:

- **Métricas Prometheus:** Expostas nativamente via Micrometer em `http://localhost:8080/actuator/prometheus`.
- **Health Check Detalhado:** Em `http://localhost:8080/actuator/health`, incluindo o indicador customizado `flux` com contagem de mensagens na DLQ e fluxos cadastrados.
- **Catálogo de Métricas:** Em `http://localhost:8080/actuator/metrics` com medições de `flux.events.ingested`, `flux.events.duplicates`, `flux.workflows.executions` e `flux.dlq.messages`.
- **Distributed Tracing (OpenTelemetry + W3C):** Propagação do cabeçalho oficial `traceparent` no Kafka e injeção automática de `X-Correlation-Id` e `X-Trace-Id` nas respostas HTTP.
- **Logs Estruturados:** Todas as linhas de log trazem `[root-l-flux,traceId,spanId]` correlacionadas via MDC do Logback/SLF4J.
- **Console Web Integrada:** Aba dedicada **📊 Observabilidade** na interface web (`http://localhost:8080/`) com métricas da JVM, uptime e saúde do sistema com atualização em um clique.

---

## Testes de Carga e Estresse

O projeto já vem pronto para ser estressado ao limite, tanto via JUnit automatizado quanto via script CLI Python com métricas detalhadas (RPS, latência P50, P90, P95, P99).

### 1. Rodar a suíte pesada em Java (JUnit 5)
Testa ingestão massiva de 250 eventos simultâneos, condição extrema de corrida de idempotência (40 threads disparando no mesmo milissegundo) e orquestração de 30 workflows concorrentes:

```bash
.\mvnw.cmd test -Dtest=HeavyLoadStressTest
```

### 2. Rodar o teste de carga interativo em Python
Dispara rajadas configuráveis contra a API em execução, sem precisar instalar nenhuma biblioteca externa:

```bash
# Alta vazão: 1.000 requisições com 50 threads concorrentes
python tests/stress_test.py --requests 1000 --concurrency 50 --mode ingest

# Corrida de Idempotência: centenas de threads colidindo na mesma chave
python tests/stress_test.py --requests 500 --concurrency 50 --mode idempotency

# Sobrecarga para DLQ: injeção de poison pills para validação de retries e desvio
python tests/stress_test.py --requests 100 --concurrency 10 --mode dlq
```

---

## Estrutura do Código

O projeto segue Arquitetura Hexagonal (*Ports & Adapters*) bem dividida:

```text
src/main/java/com/rootl/flux/
├── domain/            # Modelos e regras de negócio sem frameworks (Event, FlowDefinition, FlowExecution, RetryPolicy)
├── application/       # Casos de uso e portas (IngestEventUseCase, ManageFlowDefinitionUseCase, ReplayExecutionUseCase)
└── infrastructure/    # Adaptadores concretos (REST controllers, JPA, Kafka Producer/Consumer, DLQ e Console Web)
```

---

## Licença

Desenvolvido sob a licença [Apache 2.0](LICENSE).
