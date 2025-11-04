package gigabank.accountmanagement.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name("transactions")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsDLTTopic() {
        return TopicBuilder.name("transactions.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsRetryTopic() {
        return TopicBuilder.name("transactions.retry")
                .partitions(1)
                .replicas(1)
                .build();
    }
}