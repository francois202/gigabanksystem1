package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;

import gigabank.accountmanagement.service.Generate.TransactionTest;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static gigabank.accountmanagement.entity.TransactionType.*;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
public class AnalyticsService {
    private LocalDateTime minusMonth = LocalDateTime.now().minus(1L, ChronoUnit.MONTHS);

    private TransactionService transactionService = new TransactionService();

    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccount - счет
     * @param category    - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal sum = BigDecimal.ZERO;

        if (bankAccount == null || !transactionService.isValidCategory(category)) {
            return BigDecimal.ZERO;
        }

        sum = bankAccount.getTransactions().stream()
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> StringUtils.equals(transaction.getCategory(), category))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .map(transaction -> transaction.getValue())
                .reduce(sum, BigDecimal::add);

        return sum;
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
        Map<String, BigDecimal> categorySum = new HashMap<>();
        Set<String> validateCategories = transactionService.validateCategories(categories);

        if (user == null || validateCategories.size() == 0) {
            return categorySum;
        }


        categorySum = user.getBankAccounts().stream()
                .map(BankAccount::getTransactions)
                .flatMap(List::stream)
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> validateCategories.contains(transaction.getCategory()))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getValue, BigDecimal::add)));

        return categorySum;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> result = new LinkedHashMap<>();

        if (user == null) {
            return result;
        }

        result = user.getBankAccounts().stream()
                .map(BankAccount::getTransactions)
                .flatMap(List::stream)
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .collect(Collectors.groupingBy(Transaction::getCategory, LinkedHashMap::new, Collectors.toList()));

        return result;
    }

    public LinkedHashMap<LocalDateTime, Transaction> getTransactionListByIdentification(User user, int num) {
        LinkedHashMap<LocalDateTime, Transaction> result = new LinkedHashMap<>();

        if (user == null) {
            return result;
        }

        LinkedHashMap<LocalDateTime, Transaction> collect = user.getBankAccounts().stream()
                .map(BankAccount::getTransactions)
                .flatMap(Collection::stream)
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .sorted(Comparator.comparing(transaction -> transaction.getCreatedDate().getDayOfMonth(), Comparator.reverseOrder()))
                .limit(num)
                .collect(Collectors.toMap(Transaction::getCreatedDate, Function.identity(),
                        (existingValue, newValue) -> existingValue, LinkedHashMap::new));


        return collect;
    }

    public PriorityQueue<Transaction> getLargestUserTransaction(User user, int num) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(transaction -> transaction.getValue(),
                Comparator.reverseOrder()));

        if (user == null) {
            return result;
        }

        result = user.getBankAccounts().stream()
                .map(BankAccount::getTransactions)
                .flatMap(Collection::stream)
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(num)
                .collect(Collectors.toCollection(
                        () -> new PriorityQueue<>(Comparator.comparing(Transaction::getValue).reversed())
                ));

        return result;
    }

    /**
     * Анализ выполнения стримов последовательно и параллельно
     */

    public void analyzePerformances(LinkedList<TransactionTest> transactions) {
        long withoutParallel = analyzeWithoutParallel(transactions);
        long WithParallel = analyzeWithParallel(transactions);

        System.out.println("Program was completed: " + withoutParallel + " without parallel");
        System.out.println("Program was completed: " + WithParallel + " with parallel");
    }

    private long analyzeWithoutParallel(LinkedList<TransactionTest> transactions) {
        long start = System.currentTimeMillis();
        BigDecimal result = BigDecimal.ZERO;

        result = transactions.stream()
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .sorted(Comparator.comparing(TransactionTest::getValue).reversed())
                .map(TransactionTest::getValue)
                .reduce(result, BigDecimal::add);

        long end = System.currentTimeMillis();

        return end - start;
    }

    private long analyzeWithParallel(LinkedList<TransactionTest> transactions) {
        long start = System.currentTimeMillis();
        BigDecimal result = BigDecimal.ZERO;

        result = transactions.stream()
                .parallel()
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .sorted(Comparator.comparing(TransactionTest::getValue).reversed())
                .map(TransactionTest::getValue)
                .reduce(result, BigDecimal::add);

        long end = System.currentTimeMillis();

        return (end - start);
    }
}
