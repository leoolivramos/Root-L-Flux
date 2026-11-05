package com.rootl.flux.infrastructure.adapter.in.messaging.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public KafkaEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${flux.kafka.topics.events-raw:root-l-flux.events.raw}",
            groupId = "${spring.kafka.consumer.group-id:root-l-flux-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record) {
        log.info("[ConsumidorKafka] Mensagem recebida: chave='{}' partição={} offset={}",
                record.key(), record.partition(), record.offset());

        try {
            JsonNode root = objectMapper.readTree(record.value());
            String type = root.path("type").asText("desconhecido");
            String payload = root.path("payload").asText("");

            // Verificação de simulação de falha para teste de resiliência e DLQ
            if (payload.contains("\"simular_erro\":true") || payload.contains("\"forcar_dlq\":true")) {
                log.error("[ConsumidorKafka] Simulação de erro detectada no payload. Lançando exceção para ativar política de retries.");
                throw new IllegalStateException("Falha crítica simulada para verificação de retries e envio à DLQ");
            }

            processedCount.incrementAndGet();
            log.info("[ConsumidorKafka] Evento processado com êxito: chave='{}' tipo='{}'", record.key(), type);

        } catch (IllegalStateException e) {
            // Repassada para acionar o DefaultErrorHandler e retries
            throw e;
        } catch (Exception e) {
            log.error("[ConsumidorKafka] Erro inesperado ao desserializar/processar evento: {}", e.getMessage());
            throw new RuntimeException("Erro no processamento da mensagem do Kafka", e);
        }
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public void resetCount() {
        processedCount.set(0);
    }
}
