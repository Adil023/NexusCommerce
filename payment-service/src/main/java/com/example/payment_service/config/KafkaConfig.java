package com.example.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic createRefundTopic() {
        return TopicBuilder.name("refund-topic-v2")
                .partitions(1)
                .replicas(1)
                .build();
    }
}