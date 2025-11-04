package gigabank.accountmanagement.kafka.consumer;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.kafka.producer.DeadLetterTopicProducer;
import gigabank.accountmanagement.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TransactionKafkaConsumer {

    @Value("${app.kafka.consumer-groups.at-most-once}")
    private String atMostOnceGroupId;

    @Value("${app.kafka.consumer-groups.at-least-once}")
    private String atLeastOnceGroupId;

    @Value("${app.kafka.consumer-groups.exactly-once}")
    private String exactlyOnceGroupId;

    @Value("${app.kafka.consumer-groups.retry-dlt}")
    private String retryDltGroupId;

    private final TransactionService transactionService;
    private final DeadLetterTopicProducer deadLetterTopicProducer;
    private final ConcurrentHashMap<Long, Boolean> processedTransactions;

    public TransactionKafkaConsumer(TransactionService transactionService,
                                    DeadLetterTopicProducer deadLetterTopicProducer) {
        this.transactionService = transactionService;
        this.deadLetterTopicProducer = deadLetterTopicProducer;
        this.processedTransactions = new ConcurrentHashMap<>();
    }

    /**
     * At-most-once консьюмер
     */
    @KafkaListener(
            topics = "transactions",
            groupId = "${app.kafka.consumer-groups.at-most-once}",
            containerFactory = "atMostOnceContainerFactory"
    )
    public void consumeAtMostOnce(TransactionMessage transaction) {
        try {
            log.info("[At-Most-Once] Получена транзакция: transactionId={}", transaction.getId());
            transactionService.processTransaction(transaction, "at-most-once");
            log.info("[At-Most-Once] Транзакция обработана: transactionId={}", transaction.getId());
        } catch (Exception e) {
            log.error("[At-Most-Once] Ошибка обработки: transactionId={}", transaction.getId(), e);
        }
    }

    /**
     * At-least-once консьюмер
     */
    @KafkaListener(
            topics = "transactions",
            groupId = "${app.kafka.consumer-groups.at-least-once}",
            containerFactory = "atLeastOnceContainerFactory"
    )
    public void consumeAtLeastOnce(ConsumerRecord<String, TransactionMessage> record,
                                   Acknowledgment ack) {
        TransactionMessage transaction = record.value();

        try {
            log.info("[At-Least-Once] Получена транзакция: transactionId={}", transaction.getId());
            transactionService.processTransaction(transaction, "at-least-once");
            ack.acknowledge();
            log.info("[At-Least-Once] Транзакция обработана: transactionId={}", transaction.getId());
        } catch (Exception e) {
            log.error("[At-Least-Once] Ошибка обработки: transactionId={}", transaction.getId(), e);
            throw e;
        }
    }

    /**
     * Exactly-once консьюмер
     */
    @KafkaListener(
            topics = "transactions",
            groupId = "${app.kafka.consumer-groups.exactly-once}",
            containerFactory = "exactlyOnceContainerFactory"
    )
    public void consumeExactlyOnce(ConsumerRecord<String, TransactionMessage> record,
                                   Acknowledgment ack) {
        TransactionMessage transaction = record.value();
        Long transactionId = transaction.getId();

        try {
            log.info("[Exactly-Once] Получена транзакция: transactionId={}", transactionId);

            if (processedTransactions.containsKey(transactionId)) {
                log.info("[Exactly-Once] Дубликат пропущен: transactionId={}", transactionId);
                ack.acknowledge();
                return;
            }

            transactionService.processTransaction(transaction, "exactly-once");
            processedTransactions.put(transactionId, true);
            ack.acknowledge();

            log.info("[Exactly-Once] Транзакция обработана: transactionId={}", transactionId);

        } catch (Exception e) {
            log.error("[Exactly-Once] Ошибка обработки: transactionId={}", transactionId, e);
            processedTransactions.remove(transactionId);
            throw e;
        }
    }

    /**
     * Retry + DLT консьюмер
     */
    @KafkaListener(
            topics = "transactions",
            groupId = "${app.kafka.consumer-groups.retry-dlt}",
            containerFactory = "retryDltContainerFactory"
    )
    public void consumeWithRetryAndDlt(ConsumerRecord<String, TransactionMessage> record,
                                       Acknowledgment ack) {
        TransactionMessage transaction = record.value();

        try {
            log.info("[Retry-DLT] Получена транзакция: transactionId={}", transaction.getId());
            transactionService.processTransaction(transaction, "at-least-once");
            ack.acknowledge();
            log.info("[Retry-DLT] Транзакция обработана: transactionId={}", transaction.getId());
        } catch (Exception e) {
            log.error("[Retry-DLT] Критическая ошибка после всех попыток: transactionId={}",
                    transaction.getId(), e);
            deadLetterTopicProducer.sendToDlt(record, e);
            ack.acknowledge();
            log.warn("[Retry-DLT] Сообщение отправлено в DLT: transactionId={}", transaction.getId());
        }
    }
}