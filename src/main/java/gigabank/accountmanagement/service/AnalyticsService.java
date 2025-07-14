package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
@RequiredArgsConstructor
public class AnalyticsService {
    private final TransactionService transactionService;

    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccount - счет
     * @param category    - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal totalSum = BigDecimal.ZERO;

        if (bankAccount == null || category.isEmpty()) {
            return totalSum;
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        totalSum = bankAccount.getTransactions().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && transaction.getCategory().equals(category)
                        && !transaction.getCreatedDate().isBefore(oneMonthAgo))
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSum;
    }

    /**
     * Вывод суммы потраченных средств на n категорий за последний месяц
     * со всех счетов пользователя
     *
     * @param user       - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories) {
        Map<String, BigDecimal> resultMap = new HashMap<>();

        if (user == null || categories.isEmpty()) {
            return resultMap;
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        resultMap = user.getBankAccounts().stream()
                .flatMap(transaction -> transaction.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && categories.contains(transaction.getCategory())
                        && !transaction.getCreatedDate().isBefore(oneMonthAgo))
                .collect(Collectors.toMap(
                        Transaction::getCategory,
                        Transaction::getValue,
                        BigDecimal::add,
                        HashMap::new));

        return resultMap;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней, отсортированные от наибольшей к наименьшей
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> resultMap = new LinkedHashMap<>();

        if (user == null) {
            return resultMap;
        }

        List<Transaction> transactionsList = user.getBankAccounts().stream()
                .flatMap(transaction -> transaction.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .toList();

        transactionsList.forEach(transaction -> resultMap.computeIfAbsent(transaction.getCategory(),
                k -> new ArrayList<>()).add(transaction));

        return resultMap;
    }

    /**
     * Вывод последних N транзакций пользователя.
     *
     * @param user - пользователь
     * @param n    - количество последних транзакций
     * @return LinkedHashMap, где ключом является идентификатор транзакции, а значением — объект Transaction
     */
    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> lastNTransaction = new ArrayList<>();

        if (user == null) {
            return lastNTransaction;
        }

        lastNTransaction = user.getBankAccounts().stream()
                .flatMap(transaction -> transaction.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());

        return lastNTransaction;
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя.
     *
     * @param user - пользователь
     * @param n    - количество топовых транзакций
     * @return PriorityQueue, где транзакции хранятся в порядке убывания их значения
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> topNLargestTransactions = new PriorityQueue<>(
                Comparator.comparing(Transaction::getValue).reversed());

        if (user == null) {
            return topNLargestTransactions;
        }

        topNLargestTransactions = user.getBankAccounts().stream()
                .flatMap(transactions -> transactions.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection(() ->
                        new PriorityQueue<>(Comparator.comparing(Transaction::getValue).reversed())));

        return topNLargestTransactions;
    }

    public List<Transaction> analyzePerformance(BankAccount bankAccount) {
        if (bankAccount == null) {
            return Collections.emptyList();
        }

        long startSequential = System.nanoTime();
        List<Transaction> transactionsSequential = bankAccount.getTransactions().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue))
                .toList();
        long endSequential = System.nanoTime() - startSequential;

        long startParallel = System.nanoTime();
        List<Transaction> transactionsParallel = bankAccount.getTransactions().parallelStream()
                .filter(transaction -> TransactionType.DEPOSIT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue))
                .toList();
        long endParallel = System.nanoTime() - startParallel;

        //Результаты работы
        System.out.println("Обычный стрим: " + endSequential);
        System.out.println("Параллельный стрим: " + endParallel);
        System.out.println("Разница: " + (endSequential - endParallel));

        return transactionsParallel;
    }
}

