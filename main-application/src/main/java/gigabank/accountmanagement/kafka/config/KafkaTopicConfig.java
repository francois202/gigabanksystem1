package gigabank.accountmanagement.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionsAtMostOnceTopic() {
        return TopicBuilder
                .name("transactions-at-most-once")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsAtLeastOnceTopic() {
        return TopicBuilder
                .name("transactions-at-least-once")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsExactlyOnceTopic() {
        return TopicBuilder
                .name("transactions-exactly-once")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsBatchTopic() {
        return TopicBuilder
                .name("transactions-batch")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsDLTTopic() {
        return TopicBuilder
                .name("transactions-retry-dlt")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsOutboxTopic() {
        return TopicBuilder
                .name("transaction-outbox-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}