package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
            return BigDecimal.ZERO;
        }
        LocalDateTime month = LocalDateTime.now().minusMonths(1);
        for (Transaction transaction : bankAccount.getTransactions()) {
            if (transaction.getCategory().equals(category) && (transaction.getType().equals(TransactionType.PAYMENT) &&
                    transaction.getCreatedDate().isAfter(month))) {

                total = total.add(transaction.getValue());


            }
        }
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
        if (user == null && transactionService.validateCategories(categories).isEmpty()) {
            return result;
        }

        LocalDateTime month = LocalDateTime.now().minusMonths(1);
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getType().equals(TransactionType.PAYMENT) && (transaction.getCreatedDate().isAfter(month))) {
                    result.put(transaction.getCategory(), transaction.getValue());
                }
            }
        }


        return result;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public TreeMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        TreeMap<String, List<Transaction>> result = new TreeMap<>();

        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getType().equals(TransactionType.PAYMENT)) {
                    transactions.add(transaction);
                }
            }
        }
        transactions.sort(Comparator.comparing(Transaction::getValue));

        for (Transaction transaction : transactions) {
            result.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }
        return result;
    }

    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> result = new ArrayList<>();
        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            transactions.addAll(bankAccount.getTransactions());
        }

        transactions.sort(Comparator.comparing(Transaction::getValue));
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(TransactionType.PAYMENT)) {
                result.add(transaction);
            }
        }
        return result;

    }

    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(Transaction::getValue));
        if (user == null && transactionService.validateCategories(Collections.emptySet()).isEmpty()) {
            return result;
        }

        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getType().equals(TransactionType.PAYMENT)) {
                    if (result.size() < n) {
                        result.offer(transaction);
                    } else if (result.peek().getValue().compareTo(transaction.getValue()) < 0) {
                        result.poll();
                        result.offer(transaction);
                    }
                }
            }
        }
        return result;

    }
}
