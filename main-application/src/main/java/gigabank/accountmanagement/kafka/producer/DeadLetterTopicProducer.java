package gigabank.accountmanagement.kafka.producer;

import gigabank.accountmanagement.dto.kafka.DeadLetterMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeadLetterTopicProducer {

    private final KafkaTemplate<String, DeadLetterMessage> kafkaTemplate;

    public DeadLetterTopicProducer(KafkaTemplate<String, DeadLetterMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToDlt(ConsumerRecord<?, ?> record, Exception exception) {
        try {
            DeadLetterMessage dltMessage = new DeadLetterMessage(
                    record.value(),
                    exception,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    "transaction-service-retry-dlt"
            );

            kafkaTemplate.send("transactions-retry-dlt", dltMessage)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Сообщение отправлено в DLT: partition={}, offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            log.error("Ошибка отправки в DLT", ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Критическая ошибка при отправке в DLT", e);
        }
    }
}
