package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.dto.request.TransactionGenerateRequest;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.kafka.producer.TransactionKafkaProducer;
import gigabank.accountmanagement.mapper.TransactionMapper;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionMapper transactionMapper;
    private final MetricsService metricsService;
    private final ConcurrentHashMap<Long, Boolean> processedTransactions = new ConcurrentHashMap<>();

    private final TransactionKafkaProducer transactionKafkaProducer;
    private final Random random = new Random();

    @Transactional
    public boolean processTransaction(TransactionMessage transactionMessage, String deliveryMode) {
        long startTime = System.currentTimeMillis();
        Long transactionId = transactionMessage.getId();
        Long accountId = transactionMessage.getBankAccountId();

        if ("exactly-once".equals(deliveryMode) && processedTransactions.containsKey(transactionId)) {
            log.info("Транзакция уже обработана: transactionId={}, accountId={}", transactionId, accountId);
            return true;
        }

        try {
            BankAccountEntity account = bankAccountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Счет не найден: " + accountId));

            if (account.isBlocked()) {
                throw new IllegalArgumentException("Счет заблокирован: " + accountId);
            }

            TransactionEntity transactionEntity = transactionMapper.toEntity(transactionMessage);
            transactionEntity.setBankAccountEntity(account);

            updateAccountBalance(account, transactionMessage);
            bankAccountRepository.save(account);
            transactionRepository.save(transactionEntity);

            if ("exactly-once".equals(deliveryMode)) {
                processedTransactions.put(transactionId, true);
            }

            long duration = System.currentTimeMillis() - startTime;
            metricsService.getSingleProcessingMetrics().incrementSuccessfulTransactions();
            metricsService.recordSingleTransactionTime(duration);

            log.info("[Single] Транзакция обработана: transactionId={}, duration={}ms",
                    transactionId, duration);
            return true;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.getSingleProcessingMetrics().incrementFailedTransactions();

            log.error("[Single] Ошибка обработки транзакции: transactionId={}, duration={}ms",
                    transactionId, duration, e);
            throw e;
        }
    }

    @Transactional
    public List<TransactionGenerateRequest> generateTransactions(int count, String deliveryMode) {
        List<TransactionGenerateRequest> transactions = new ArrayList<>();

        List<BankAccountEntity> existingAccounts = bankAccountRepository.findAll();
        if (existingAccounts.isEmpty()) {
            throw new IllegalArgumentException("В базе данных нет счетов");
        }

        for (int i = 0; i < count; i++) {
            BankAccountEntity randomAccount = existingAccounts.get(random.nextInt(existingAccounts.size()));
            TransactionGenerateRequest transactionGenerateRequest = new TransactionGenerateRequest();

            transactionGenerateRequest.setTransactionId(random.nextLong(1000) + 100);
            transactionGenerateRequest.setAccountId(randomAccount.getId());
            transactionGenerateRequest.setTransactionType(random.nextBoolean() ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL);
            transactionGenerateRequest.setAmount(BigDecimal.valueOf(10 + (random.nextDouble() * 990))
                    .setScale(2, RoundingMode.HALF_UP));

            transactions.add(transactionGenerateRequest);

            TransactionEntity transactionEntity = transactionMapper.toEntity(transactionGenerateRequest);
            transactionEntity.setBankAccountEntity(randomAccount);
            transactionRepository.save(transactionEntity);

            TransactionMessage message = transactionMapper.toMessage(transactionEntity);

            transactionKafkaProducer.sendWithDeliveryMode(message, transactionGenerateRequest.getAccountId().toString(), deliveryMode);
        }

        log.info("Сгенерировано {} транзакций.", count);
        return transactions;
    }

    @Transactional
    public boolean processTransactionBatch(List<TransactionMessage> transactions, String deliveryMode) {
        long batchStartTime = System.currentTimeMillis();
        int batchSize = transactions.size();
        int successfulTransactions = 0;

        log.info("Начало обработки батча: {} транзакций, режим: {}", batchSize, deliveryMode);

        try {
            List<Long> accountIds = transactions.stream()
                    .map(TransactionMessage::getBankAccountId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, BankAccountEntity> accountsMap = bankAccountRepository.findAllById(accountIds)
                    .stream()
                    .collect(Collectors.toMap(BankAccountEntity::getId, account -> account));

            for (TransactionMessage transaction : transactions) {
                try {
                    if (processSingleTransactionInBatch(transaction, deliveryMode, accountsMap)) {
                        successfulTransactions++;
                    }
                } catch (Exception e) {
                    log.warn("Транзакция пропущена в батче: transactionId={}, error: {}",
                            transaction.getId(), e.getMessage());
                }
            }

            long batchDuration = System.currentTimeMillis() - batchStartTime;
            metricsService.getBatchProcessingMetrics().incrementSuccessfulBatches();
            metricsService.recordBatchProcessingTime(batchDuration, batchSize);

            log.info("[Batch] Батч обработан: successful={}/{} transactions, duration={}ms, successRate={}%",
                    successfulTransactions, batchSize, batchDuration,
                    (successfulTransactions * 100) / batchSize);

            return successfulTransactions > 0;

        } catch (Exception e) {
            long batchDuration = System.currentTimeMillis() - batchStartTime;
            metricsService.getBatchProcessingMetrics().incrementFailedBatches();
            log.error("[Batch] Критическая ошибка обработки батча: transactions={}, duration={}ms",
                    batchSize, batchDuration, e);
            throw new RuntimeException("Batch processing failed", e);
        }
    }

    private boolean processSingleTransactionInBatch(TransactionMessage transaction, String deliveryMode,
                                                    Map<Long, BankAccountEntity> accountsMap) {
        Long transactionId = transaction.getId();
        Long accountId = transaction.getBankAccountId();

        try {
            if ("exactly-once".equals(deliveryMode) && processedTransactions.containsKey(transactionId)) {
                log.debug("Дубликат в батче: transactionId={}", transactionId);
                metricsService.getBatchProcessingMetrics().incrementDuplicateTransactions();
                return true;
            }

            BankAccountEntity account = accountsMap.get(accountId);
            if (account == null) {
                log.warn("Счет не найден, пропускаем транзакцию: transactionId={}, accountId={}",
                        transactionId, accountId);
                metricsService.getBatchProcessingMetrics().incrementFailedTransactions();
                return false;
            }

            if (account.isBlocked()) {
                log.warn("Счет заблокирован, пропускаем транзакцию: transactionId={}, accountId={}",
                        transactionId, accountId);
                metricsService.getBatchProcessingMetrics().incrementFailedTransactions();
                return false;
            }

            TransactionEntity transactionEntity = transactionMapper.toEntity(transaction);
            transactionEntity.setBankAccountEntity(account);

            updateAccountBalance(account, transaction);
            transactionRepository.save(transactionEntity);

            if ("exactly-once".equals(deliveryMode)) {
                processedTransactions.put(transactionId, true);
            }

            metricsService.getBatchProcessingMetrics().incrementSuccessfulTransactions();
            return true;

        } catch (Exception e) {
            log.error("Ошибка обработки транзакции в батче: transactionId={}, accountId={}",
                    transactionId, accountId, e);
            metricsService.getBatchProcessingMetrics().incrementFailedTransactions();
            return false;
        }
    }

    private void updateAccountBalance(BankAccountEntity account, TransactionMessage transaction) {
        BigDecimal amount = transaction.getValue();
        BigDecimal newBalance;

        if (transaction.getType() == TransactionType.DEPOSIT) {
            newBalance = account.getBalance().add(amount);
        } else if (transaction.getType() == TransactionType.WITHDRAWAL) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Недостаточно средств на счете: " + account.getId());
            }
            newBalance = account.getBalance().subtract(amount);
        } else {
            throw new IllegalArgumentException("Неизвестный тип транзакции: " + transaction.getType());
        }

        account.setBalance(newBalance);
    }

    /**
     * Получает все транзакции для указанного счета.
     *
     * @param accountId идентификатор счета
     * @return список DTO транзакций для указанного счета
     */
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        log.info("Получение транзакций для счета ID: {}", accountId);

        List<TransactionEntity> transactionEntities = transactionRepository.findByAccountId(accountId);

        List<TransactionResponse> response = transactionEntities.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Найдено {} транзакций для счета ID {}", response.size(), accountId);
        return response;
    }

    /**
     * Получает отфильтрованный список транзакций с поддержкой пагинации.
     *
     * @param accountId идентификатор счета для фильтрации (опционально)
     * @param type тип транзакции для фильтрации (опционально)
     * @param category категория транзакции для фильтрации (опционально)
     * @param startDate начальная дата для фильтрации по периоду (опционально)
     * @param pageable параметры пагинации и сортировки
     * @return страница с отфильтрованными транзакциями в формате DTO
     */
    public Page<TransactionResponse> getFilteredTransactions(Long accountId, TransactionType type,
                                                           String category, LocalDateTime startDate,
                                                           Pageable pageable) {
        log.info("Попытка поиска транзакций по фильтру.");
        String accountIdStr = accountId != null ? accountId.toString() : null;
        String typeStr = type != null ? type.name() : null;
        String startDateStr = startDate != null ? startDate.toString() : null;

        Page<TransactionEntity> transactionEntities = transactionRepository.findWithFiltersNative(
                accountIdStr, typeStr, category, startDateStr, pageable);

        log.info("Найдено {} транзакций", transactionEntities.getTotalElements());
        return transactionEntities.map(transactionMapper::toResponse);
    }
}
