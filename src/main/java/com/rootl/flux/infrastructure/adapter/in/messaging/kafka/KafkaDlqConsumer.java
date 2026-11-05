package com.rootl.flux.infrastructure.adapter.in.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaDlqConsumer.class);

    private final DlqMessageStore dlqMessageStore;

    public KafkaDlqConsumer(DlqMessageStore dlqMessageStore) {
        this.dlqMessageStore = dlqMessageStore;
    }

    @KafkaListener(
            topics = "${flux.kafka.topics.events-dlq:root-l-flux.events.dlq}",
            groupId = "root-l-flux-dlq-monitor-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDlq(ConsumerRecord<String, String> record) {
        String originalTopic = extractHeader(record, KafkaHeaders.DLT_ORIGINAL_TOPIC, KafkaHeaders.ORIGINAL_TOPIC, "desconhecido");
        String exceptionMessage = extractHeader(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE, KafkaHeaders.EXCEPTION_MESSAGE, "Sem mensagem de exceção");

        log.warn("[ConsumidorDLQ] Mensagem crítica recebida na Dead Letter Queue! Chave='{}' TópicoOriginal='{}' Motivo='{}'",
                record.key(), originalTopic, exceptionMessage);

        DlqMessageStore.DlqItem item = new DlqMessageStore.DlqItem(
                UUID.randomUUID().toString(),
                record.key(),
                record.value(),
                originalTopic,
                exceptionMessage,
                Instant.now()
        );

        dlqMessageStore.add(item);
    }

    private String extractHeader(ConsumerRecord<String, String> record, String primaryHeader, String secondaryHeader, String defaultValue) {
        Header header = record.headers().lastHeader(primaryHeader);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        header = record.headers().lastHeader(secondaryHeader);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        for (Header h : record.headers()) {
            if (h.key() != null && h.key().toLowerCase().contains("exception-message")) {
                return new String(h.value(), StandardCharsets.UTF_8);
            }
        }
        return defaultValue;
    }
}
