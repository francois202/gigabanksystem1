package gigabank.accountmanagement.kafka.producer;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransactionKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(TransactionKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TransactionKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Отправляет транзакцию с учетом режима доставки
     */
    public void sendWithDeliveryMode(TransactionMessage message, String accountId, String deliveryMode) {
        String topicName;
        switch (deliveryMode.toLowerCase()) {
            case "at-most-once":
                topicName = "transactions-at-most-once";
                sendAtMostOnce(message, accountId, topicName);
                break;
            case "at-least-once":
                topicName = "transactions-at-least-once";
                sendAtLeastOnce(message, accountId, topicName);
                break;
            case "exactly-once":
                topicName = "transactions-exactly-once";
                sendExactlyOnce(message, accountId, topicName);
                break;
            case "batch":
                topicName = "transactions-batch";
                sendAtLeastOnce(message, accountId, topicName);
                break;
            default:
                topicName = "transactions-at-least-once";
                sendAtLeastOnce(message, accountId, topicName);
        }
    }

    private void sendAtMostOnce(TransactionMessage transactionMessage, String accountId, String topicName) {
        try {
            kafkaTemplate.send(topicName, accountId, transactionMessage);
        }
        catch (Exception e) {
            log.error("At-most-once: Не удалось отправить транзакцию: accountId={}", accountId, e);
        }
    }

    private void sendAtLeastOnce(TransactionMessage message, String accountId, String topicName) {
        String accountKey = String.valueOf(accountId);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, accountKey, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("At-least-once: Transaction delivered: accountId={}, partition={}, offset={}",
                        accountKey,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.warn("At-least-once:  Failed to send, will retry: accountId={}, error: {}",
                        accountKey, ex.getMessage());
            }
        });
    }

    /**
     * Exactly-once: идемпотентная отправка
     */
    private void sendExactlyOnce(TransactionMessage message, String accountId, String topicName) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topicName, accountId, message).get();

            log.info("Exactly-once: Transaction delivered exactly once: accountId={}, partition={}, offset={}",
                    accountId,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Exactly-once: Transaction failed: accountId={}", accountId, e);
        }
    }
}
