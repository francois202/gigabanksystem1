package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.kafka.producer.TransactionKafkaProducer;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionLoadService {

    private final TransactionKafkaProducer kafkaProducer;
    private final BankAccountRepository bankAccountRepository;

    /**
     * Генерация нагрузки для тестирования обработки транзакций
     */
    @Transactional(readOnly = true)
    public String generateBatchLoad(int totalTransactions, String deliveryMode) {
        List<BankAccountEntity> existingAccounts = bankAccountRepository.findAll();
        if (existingAccounts.isEmpty()) {
            return "В базе данных нет счетов.";
        }

        List<Long> existingAccountIds = existingAccounts.stream()
                .map(BankAccountEntity::getId)
                .toList();

        long baseId = System.currentTimeMillis();
        int successfulSends = 0;

        for (int i = 0; i < totalTransactions; i++) {
            try {
                TransactionMessage message = createRandomTransaction(baseId + i, existingAccountIds);
                kafkaProducer.sendWithDeliveryMode(message,
                        message.getBankAccountId().toString(),
                        deliveryMode);
                successfulSends++;
            } catch (Exception e) {
                log.warn("Не удалось отправить транзакцию {}/{}: {}",
                        i + 1, totalTransactions, e.getMessage());
            }
        }

        String result = String.format("Отправлено %d/%d транзакций для %d счетов в режиме %s",
                successfulSends, totalTransactions, existingAccountIds.size(), deliveryMode);

        log.info(result);
        return result;
    }

    /**
     * Создание случайной транзакции для тестирования
     */
    public TransactionMessage createRandomTransaction(Long transactionId, List<Long> availableAccountIds) {
        TransactionMessage message = new TransactionMessage();
        message.setId(transactionId);

        Long randomAccountId = availableAccountIds.get(
                ThreadLocalRandom.current().nextInt(availableAccountIds.size())
        );
        message.setBankAccountId(randomAccountId);

        message.setType(ThreadLocalRandom.current().nextBoolean() ?
                TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);

        message.setValue(BigDecimal.valueOf(
                ThreadLocalRandom.current().nextDouble(10, 1000)
        ));

        message.setCreatedDate(LocalDateTime.now());

        return message;
    }
}