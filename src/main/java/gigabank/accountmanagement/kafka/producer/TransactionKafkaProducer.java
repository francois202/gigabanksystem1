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
        switch (deliveryMode.toLowerCase()) {
            case "at-most-once":
                sendAtMostOnce(message, accountId);
                break;
            case "at-least-once":
                sendAtLeastOnce(message, accountId);
                break;
            case "exactly-once":
                sendExactlyOnce(message, accountId);
                break;
            default:
                sendAtLeastOnce(message, accountId);
        }
    }

    private void sendAtMostOnce(TransactionMessage transactionMessage, String accountId) {
        try {
            kafkaTemplate.send("transactions", accountId, transactionMessage);
        }
        catch (Exception e) {
            log.error("At-most-once: Не удалось отправить транзакцию: accountId={}", accountId, e);
        }
    }

    private void sendAtLeastOnce(TransactionMessage message, String accountId) {
        String accountKey = String.valueOf(accountId);
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("transactions", accountKey, message);

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
    private void sendExactlyOnce(TransactionMessage message, String accountId) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send("transactions", accountId, message).get();

            log.info("Exactly-once: Transaction delivered exactly once: accountId={}, partition={}, offset={}",
                    accountId,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (Exception e) {
            log.error("Exactly-once: Transaction failed: accountId={}", accountId, e);
        }
    }

    public void sendTransactionToKafka(TransactionMessage transactionMessage) {
        String accountId = String.valueOf(transactionMessage.getBankAccountId());

        kafkaTemplate.send("transactions", accountId, transactionMessage);

        log.info("Transaction sent to kafka: id={}, accountId={}", transactionMessage.getId(), accountId);
    }
}
