package gigabank.accountmanagement.kafka.consumer;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchTransactionConsumer {

    private final TransactionService transactionService;

    /**
     * Батч консьюмер для пакетной обработки
     */
    @KafkaListener(
            topics = "transactions-batch",
            groupId = "${app.kafka.consumer-groups.batch}",
            containerFactory = "batchContainerFactory"
    )
    public void consumeBatch(List<TransactionMessage> transactions) {
        try {
            log.info("Получен батч: {} транзакций", transactions.size());

            if (!transactions.isEmpty()) {
                boolean success = transactionService.processTransactionBatch(transactions, "batch");
                if (success) {
                    log.info("Батч успешно обработан: {} транзакций", transactions.size());
                } else {
                    log.error("Батч не обработан: {} транзакций", transactions.size());
                    throw new RuntimeException("Batch processing failed");
                }
            }

        } catch (Exception e) {
            log.error("Критическая ошибка обработки батча: {} сообщений",
                    transactions.size(), e);
            throw e;
        }
    }
}