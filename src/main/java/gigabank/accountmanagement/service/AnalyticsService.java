package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
public class AnalyticsService {

    private TransactionService transactionService;
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccount - счет
     * @param category    - категория
     */


    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {

        BigDecimal total = BigDecimal.ZERO;


        if (bankAccount == null || transactionService.isValidCategory(category)) {
            return total;
        }

        LocalDateTime monthLater = LocalDateTime.now().minusMonths(1L);

        total = bankAccount.getTransactions().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && StringUtils.equals(transaction.getCategory(), category)
                        && transaction.getCreatedDate().isAfter(monthLater))
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total;
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
        Map<String, BigDecimal> result = new HashMap<>();
        Set<String> categorySet = new HashSet<>(categories);
        if (user == null && transactionService.validateCategories(categories).isEmpty()) {
            return result;
        }

        LocalDateTime monthLater = LocalDateTime.now().minusMonths(1);

        result =  user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType())
                        && categorySet.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(monthLater))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getValue, BigDecimal::add)));


        return result;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> result = new LinkedHashMap<>();

        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue))
                .collect(Collectors.groupingBy(Transaction::getCategory, LinkedHashMap::new, Collectors.toList()));

        return result;
    }

    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> result = new ArrayList<>();
        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());

        return result;
    }

    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(Transaction::getValue));
        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection(PriorityQueue::new));

        return result;

    }

    public void analyzePerformance(User user) {
        if (user == null) {
            return;
        }

        List<Transaction> transactions = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .collect(Collectors.toList());


        long startTime = System.nanoTime();
        Map<String, BigDecimal> sequentialResults = transactions.stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getValue, BigDecimal::add)));
        long sequentialDuration = System.nanoTime() - startTime;


        startTime = System.nanoTime();
        Map<String, BigDecimal> parallelResults = transactions.parallelStream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getValue, BigDecimal::add)));
        long parallelDuration = System.nanoTime() - startTime;

        System.out.println("Время выполнения последовательного стрима: " + sequentialDuration / 1_000_000 + " мс");
        System.out.println("Время выполнения параллельного стрима: " + parallelDuration / 1_000_000 + " мс");
        System.out.println("Результаты равны: " + sequentialResults.equals(parallelResults));
    }
}
