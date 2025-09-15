package gigabank.accountmanagement.service;

import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final TransactionService transactionService;

    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccountEntity - счет
     * @param category    - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccountEntity bankAccountEntity, String category) {
        BigDecimal totalSum = BigDecimal.ZERO;

        if (bankAccountEntity == null || !transactionService.isValidCategory(category)) {
            return totalSum;
        }

        LocalDateTime oneMontAgo = LocalDateTime.now().minusMonths(1L);

        totalSum = bankAccountEntity.getTransactionEntities().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && StringUtils.equals(transaction.getCategory(), category)
                        && transaction.getCreatedDate().isAfter(oneMontAgo))
                .map(TransactionEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSum;
    }

    /**
     * Вывод суммы потраченных средств на n категорий за последний месяц
     * со всех счетов пользователя
     *
     * @param userEntity       - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(UserEntity userEntity, Set<String> categories) {
        Map<String, BigDecimal> resultMap = new HashMap<>();
        Set<String> validCategories = transactionService.validateCategories(categories);
        if (userEntity == null || validCategories.isEmpty()) {
            return resultMap;
        }

        LocalDateTime oneMontAgo = LocalDateTime.now().minusMonths(1L);

        resultMap =  userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(oneMontAgo))
                .collect(Collectors.groupingBy(TransactionEntity::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, TransactionEntity::getValue, BigDecimal::add)));

        return resultMap;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param userEntity - пользователь
     * @return мапа категория - все операции совершенные по ней, отсортированные от наибольшей к наименьшей
     */
    public LinkedHashMap<String, List<TransactionEntity>> getTransactionHistorySortedByAmount(UserEntity userEntity) {
        LinkedHashMap<String, List<TransactionEntity>> resultMap = new LinkedHashMap<>();
        if (userEntity == null) {
            return resultMap;
        }

        resultMap = userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount ->bankAccount.getTransactionEntities().stream())
                .filter(transaction ->TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(TransactionEntity::getValue))
                .collect(Collectors.groupingBy(TransactionEntity::getCategory, LinkedHashMap::new, Collectors.toList()));

        return resultMap;
    }

    /**
     * Вывод последних N транзакций пользователя.
     *
     * @param userEntity - пользователь
     * @param n    - количество последних транзакций
     * @return LinkedHashMap, где ключом является идентификатор транзакции, а значением — объект Transaction
     */
    public List<TransactionEntity> getLastNTransactions(UserEntity userEntity, int n) {
        List<TransactionEntity> lastTransactionEntities = new ArrayList<>();
        if (userEntity == null) {
            return lastTransactionEntities;
        }

        lastTransactionEntities = userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .sorted(Comparator.comparing(TransactionEntity::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());

        return lastTransactionEntities;
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя.
     *
     * @param userEntity - пользователь
     * @param n    - количество топовых транзакций
     * @return PriorityQueue, где транзакции хранятся в порядке убывания их значения
     */
    public PriorityQueue<TransactionEntity> getTopNLargestTransactions(UserEntity userEntity, int n) {
        PriorityQueue<TransactionEntity> transactionEntityPriorityQueue =
                new PriorityQueue<>(Comparator.comparing(TransactionEntity::getValue));

        if (userEntity == null) {
            return transactionEntityPriorityQueue;
        }

        transactionEntityPriorityQueue = userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(TransactionEntity::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection
                        (() -> new PriorityQueue<>(Comparator.comparing(TransactionEntity::getValue).reversed())));

        return transactionEntityPriorityQueue;
    }
}

