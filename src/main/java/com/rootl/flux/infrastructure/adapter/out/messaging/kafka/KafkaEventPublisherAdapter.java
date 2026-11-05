package com.rootl.flux.infrastructure.adapter.out.messaging.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.domain.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsTopic;

    public KafkaEventPublisherAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${flux.kafka.topics.events-raw:root-l-flux.events.raw}") String eventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.eventsTopic = eventsTopic;
    }

    @Override
    public void publish(Event event) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", event.getId().value());
            messageMap.put("type", event.getType());
            messageMap.put("source", event.getMetadata().source());
            messageMap.put("idempotencyKey", event.getMetadata().idempotencyKey());
            messageMap.put("correlationId", event.getMetadata().correlationId());
            messageMap.put("traceparent", event.getMetadata().traceparent());
            messageMap.put("timestamp", event.getMetadata().timestamp().toString());
            messageMap.put("payload", event.getPayload());
            messageMap.put("status", event.getStatus().name());

            String messagePayload = objectMapper.writeValueAsString(messageMap);
            String messageKey = event.getId().value();

            kafkaTemplate.send(eventsTopic, messageKey, messagePayload).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[PublicadorKafka] Mensagem enviada ao tópico '{}': id={} partição={} offset={}",
                            eventsTopic, messageKey,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[PublicadorKafka] Falha ao enviar mensagem ao tópico '{}': id={} erro={}",
                            eventsTopic, messageKey, ex.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("[PublicadorKafka] Erro de serialização JSON do evento id={}: {}",
                    event.getId().value(), e.getMessage());
            throw new RuntimeException("Falha na serialização do evento para o Kafka", e);
        }
    }
}
