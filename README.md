# Root L Flux

> Orquestrador leve e desacoplado para processamento de fluxos de eventos distribuídos, construído com Java 21+ e Spring Boot 3.4.

O **Root L Flux** foi feito para resolver um problema clássico de quem lida com microsserviços e mensageria: como receber eventos de vários sistemas, garantir que nenhum evento repetido faça besteira (idempotência), rodar as etapas do fluxo na ordem certa e saber exatamente o estado de cada execução sem dor de cabeça.

---

## O que ele resolve na prática

- **Ingestão com validação real:** Se o payload ou metadados essenciais estiverem errados, o sistema rejeita logo na porta de entrada com código e mensagem clara.
- **Idempotência nativa:** Chegou evento repetido com a mesma chave? Ele não reprocessa e devolve status de conflito amigável.
- **Domínio puro (Arquitetura Hexagonal):** As regras do negócio não dependem do Spring, do banco ou do broker. Se amanhã trocar o banco ou mensageria, a lógica continua intacta.
- **Pronto pro dia a dia:** Roda local com banco em memória em 5 segundos, mas já vem configurado com Docker Compose para subir PostgreSQL e Apache Kafka (KRaft) quando precisar de carga real.
- **Swagger pronto:** Subiu a aplicação, a documentação interativa já tá no ar.

---

## Tecnologias

- **Java 21+** (compatível com Java 25 e 27)
- **Spring Boot 3.4** (Web, Data JPA, Validation, Actuator)
- **PostgreSQL 16** / H2 (banco em memória para desenvolvimento ágil e testes)
- **Apache Kafka 3.8** (modo KRaft local)
- **OpenAPI 3 / Swagger UI**
- **JUnit 5 & Mockito**

---

## Como rodar localmente

### 1. Pré-requisitos
- JDK 21 ou superior instalado.
- Docker (opcional, apenas se quiser rodar PostgreSQL e Kafka locais).

### 2. Rodando a aplicação
Você não precisa nem instalar o Maven na máquina, basta usar o wrapper já incluído:

```bash
# No Windows (PowerShell):
.\mvnw.cmd spring-boot:run

# No Linux / macOS:
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

A documentação interativa (Swagger UI) fica em:
`http://localhost:8080/swagger-ui.html`

### 3. Rodando os testes
```bash
.\mvnw.cmd test
```

### 4. Subindo infraestrutura completa (PostgreSQL + Kafka)
Se quiser rodar o ambiente completo com os containers oficiais:

```bash
docker compose up -d
```

---

## Exemplos rápidos de uso

### Ingerir um evento
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

### Disparar um fluxo com o evento
```bash
curl -X POST "http://localhost:8080/api/v1/executions/trigger?flowId=fluxo-faturamento&eventId=<EVENT_ID>"
```

### Consultar o estado da execução
```bash
curl http://localhost:8080/api/v1/executions/<EXECUTION_ID>
```

---

## Estrutura do Código

O projeto segue Arquitetura Hexagonal (*Ports & Adapters*) bem dividida:

```text
src/main/java/com/rootl/flux/
├── domain/            # Modelos e regras de negócio sem frameworks (Event, FlowExecution)
├── application/       # Casos de uso e portas (IngestEventUseCase, repositórios, mensageria)
└── infrastructure/    # Adaptadores concretos (REST controllers, JPA, Kafka e configurações)
```

---

## Licença

Desenvolvido sob a licença [Apache 2.0](LICENSE).
