package com.rootl.flux.infrastructure.adapter.messaging.kafka;

import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventMetadata;
import com.rootl.flux.infrastructure.adapter.in.messaging.kafka.DlqMessageStore;
import com.rootl.flux.infrastructure.adapter.in.messaging.kafka.KafkaEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"root-l-flux.events.raw.test", "root-l-flux.events.dlq.test"}
)
@ActiveProfiles("test")
@DirtiesContext
class KafkaEndToEndIntegrationTest {

    @Autowired
    private EventPublisherPort eventPublisherPort;

    @Autowired
    private KafkaEventConsumer kafkaEventConsumer;

    @Autowired
    private DlqMessageStore dlqMessageStore;

    @BeforeEach
    void setUp() {
        kafkaEventConsumer.resetCount();
        dlqMessageStore.clear();
    }

    @Test
    @DisplayName("Deve publicar evento no Kafka e consumir com sucesso ponta a ponta")
    void shouldPublishAndConsumeEventSuccessfully() {
        EventMetadata metadata = EventMetadata.create("servico-pagamentos", "chave-pg-1", "corr-pg-1", null);
        Event event = Event.create("pagamento.autorizado", metadata, "{\"pedidoId\":\"ped-100\",\"valor\":99.90}");

        eventPublisherPort.publish(event);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertTrue(kafkaEventConsumer.getProcessedCount() >= 1,
                        "O consumidor Kafka deve processar a mensagem com sucesso"));
    }

    @Test
    @DisplayName("Deve executar retries e rotear para Dead Letter Queue (DLQ) em caso de falha persistente")
    void shouldRouteToDeadLetterQueueWhenRetriesAreExhausted() {
        EventMetadata metadata = EventMetadata.create("servico-pedidos", "chave-falha-1", "corr-falha-1", null);
        // Payload com flag forcar_dlq para simular erro irrecuperável
        Event eventComFalha = Event.create("pedido.critico", metadata, "{\"pedidoId\":\"ped-err-1\",\"forcar_dlq\":true}");

        eventPublisherPort.publish(eventComFalha);

        // Aguarda a expiração dos retries e o roteamento automático para o tópico da DLQ
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    assertEquals(1, dlqMessageStore.size(), "A mensagem deve ser capturada na DLQ");
                    var item = dlqMessageStore.getAll().get(0);
                    assertTrue(item.payload().contains("forcar_dlq"), "O payload original deve ser preservado na DLQ");
                    assertTrue(item.exceptionMessage().contains("Falha crítica simulada"), "A causa da falha deve constar na mensagem da DLQ");
                });
    }
}
