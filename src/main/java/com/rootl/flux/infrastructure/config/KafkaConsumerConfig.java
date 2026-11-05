package com.rootl.flux.infrastructure.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${flux.kafka.topics.events-dlq:root-l-flux.events.dlq}")
    private String dlqTopic;

    @Value("${flux.kafka.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${flux.kafka.retry.backoff-interval-ms:1000}")
    private long backoffIntervalMs;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (record, ex) -> {
                    log.warn("[KafkaResiliência] Esgotadas as tentativas ({}/{}). Roteando mensagem com chave '{}' para a DLQ '{}'. Motivo: {}",
                            maxAttempts, maxAttempts, record.key(), dlqTopic, ex.getMessage());
                    return new TopicPartition(dlqTopic, -1);
                });

        FixedBackOff backOff = new FixedBackOff(backoffIntervalMs, maxAttempts - 1);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.info("[KafkaResiliência] Tentativa {}/{} de processamento da mensagem na partição {} com offset {}",
                        deliveryAttempt, maxAttempts, record.partition(), record.offset())
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
