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

    private final TransactionService TRANSACTION_SERVICE;
    private final LocalDateTime ONE_MONTH = LocalDateTime.now().minusMonths(1L);
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     * @param bankAccount - счет
     * @param category - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category){
        BigDecimal amount = BigDecimal.ZERO;
        if (bankAccount == null || !TRANSACTION_SERVICE.isValidCategories(category))
            return amount;

        amount = bankAccount.getTransactions().stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> StringUtils.equals(transaction.getCategory(), category))
                .filter(transaction -> transaction.getCreatedDate().isAfter(ONE_MONTH))
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

         return amount;
    }

    /**
     * Вывод суммы потраченных средств на n категорий за последний месяц
     * со всех счетов пользователя
     *
     * @param user - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories){
        Map<String, BigDecimal> result = new HashMap<>();
        Set<String> validCategories = TRANSACTION_SERVICE.validateCategories(categories);
        if (user == null || validCategories.isEmpty())
            return result;

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> validCategories.contains(transaction.getCategory()))
                .filter(transaction -> transaction.getCreatedDate().isAfter(ONE_MONTH))
                .collect(Collectors.toMap(Transaction::getCategory, Transaction::getValue, BigDecimal::add));

        return result;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user){
        LinkedHashMap<String, List<Transaction>> result = new LinkedHashMap<>();
        if (user == null)
            return result;

        result = user.getBankAccounts().stream()
                        .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                        .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                        .sorted(Comparator.comparing(Transaction::getValue))
                        .collect(Collectors.groupingBy(Transaction::getCategory, LinkedHashMap::new, Collectors.toList()));

        return result;
    }

    /**
     *  Вывод последних N транзакций пользователя
     * @param user - пользователь
     * @param n - кол-во последних транзакций
     */
    public List<Transaction> getLastNTransaction(User user, int n){
        List<Transaction> allTransaction = new ArrayList<>();
        List<Transaction> result = new ArrayList<>();

        if (user == null)
            return result;

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Вывод топ-N самых больших платежных  транзакций пользователя
     * @param user - пользователь
     * @param n - кол-во последних транзакций
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n){
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(Transaction::getValue));

        if (user == null)
            return result;

        result = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection(() -> new PriorityQueue<>(Comparator.comparing(Transaction::getValue).reversed())));

        return result;
    }

    public long analyzePerformance(List<Transaction> transactions){

        if(transactions.isEmpty())
            return 0L;

        long startTime = System.nanoTime();
        List<Transaction> consistent = transactions.stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getCreatedDate().isAfter(ONE_MONTH))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("10.00")) > 0)
                .sorted(Comparator.comparing(Transaction::getValue))
                .sorted(Comparator.comparing(Transaction::getCreatedDate))
                .sorted(Comparator.comparing(Transaction::getCategory))
                .toList();
        long endTime = System.nanoTime();

        long consistentTime = endTime - startTime;

        startTime = System.nanoTime();
        List<Transaction> concurrent = transactions.stream().parallel()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getCreatedDate().isAfter(ONE_MONTH))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("10.00")) > 0)
                .sorted(Comparator.comparing(Transaction::getValue))
                .sorted(Comparator.comparing(Transaction::getCreatedDate))
                .sorted(Comparator.comparing(Transaction::getCategory))
                .toList();
        endTime = System.nanoTime();

        long concurrentTime = endTime - startTime;

        return consistentTime - concurrentTime;
    }


}
