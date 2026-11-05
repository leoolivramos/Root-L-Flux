package com.rootl.flux.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${flux.kafka.topics.events-raw:root-l-flux.events.raw}")
    private String eventsRawTopic;

    @Value("${flux.kafka.topics.events-dlq:root-l-flux.events.dlq}")
    private String eventsDlqTopic;

    @Bean
    public NewTopic eventsRawTopic() {
        return TopicBuilder.name(eventsRawTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic eventsDlqTopic() {
        return TopicBuilder.name(eventsDlqTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
