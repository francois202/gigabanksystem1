package gigabank.accountmanagement.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> atMostOnceContainerFactory() {
        return createContainerFactory(true, ContainerProperties.AckMode.BATCH);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> atLeastOnceContainerFactory() {
        return createContainerFactory(false, ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> exactlyOnceContainerFactory() {
        var factory = createContainerFactory(false, ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        Map<String, Object> props = getBaseConsumerProperties();
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> retryDltContainerFactory() {
        var factory = createContainerFactory(false, ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> outboxEventsContainerFactory() {
        return createContainerFactory(false, ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        Map<String, Object> props = getBaseConsumerProperties();

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 20);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        factory.setCommonErrorHandler(batchErrorHandler());

        return factory;
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> createContainerFactory(
            boolean autoCommit, ContainerProperties.AckMode ackMode) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        Map<String, Object> props = getBaseConsumerProperties();

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        if (autoCommit) {
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        }

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.getContainerProperties().setAckMode(ackMode);

        return factory;
    }

    private Map<String, Object> getBaseConsumerProperties() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers() != null ?
                        kafkaProperties.getBootstrapServers() : "localhost:9092");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "gigabank.accountmanagement.dto.kafka.TransactionMessage");

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 40000);

        return props;
    }

    private CommonErrorHandler errorHandler() {
        var backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(8000L);
        backOff.setMaxElapsedTime(15000L);

        return new DefaultErrorHandler((record, exception) -> {}, backOff);
    }

    private CommonErrorHandler batchErrorHandler() {
        var backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(8000L);

        var errorHandler = new DefaultErrorHandler((record, exception) -> {}, backOff);

        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}