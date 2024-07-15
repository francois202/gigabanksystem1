package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
public class AnalyticsService {
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     * @param bankAccount - счет
     * @param category - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal sum = BigDecimal.ZERO;
        if (bankAccount == null || category == null
                || !TransactionService.TRANSACTION_CATEGORIES.contains(category)) {
            return sum;
        }

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime lastMonth = today.minus(1L, ChronoUnit.MONTHS);

        for (Transaction transaction : bankAccount.getTransactions()) {
            if (transaction.getCreatedDate().isAfter(lastMonth)
                    && StringUtils.equals(transaction.getCategory(), category)
                    && TransactionType.PAYMENT.equals(transaction.getType())) {

                sum = sum.add(transaction.getValue());
            }
        }
        return sum;
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
        Map<String, BigDecimal> monthlySpendingMap = new HashMap<>();
        if (user == null || categories == null) {
            return monthlySpendingMap;
        }

        Set<String> filteredCategories = categories.stream()
                .filter(c -> TransactionService.TRANSACTION_CATEGORIES.contains(c))
                .collect(Collectors.toCollection(HashSet::new));

        if (filteredCategories.isEmpty()) {
            return monthlySpendingMap;
        }

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime lastMonth = today.minus(1L, ChronoUnit.MONTHS);

        monthlySpendingMap = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .filter(t -> t.getCreatedDate().isAfter(lastMonth) && TransactionType.PAYMENT.equals(t.getType()))
                .collect(Collectors.toMap(Transaction::getCategory, Transaction::getValue, BigDecimal::add));

        return monthlySpendingMap;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> transactionsMap = new LinkedHashMap<>();
        List<Transaction> transactionsValueDesc = new ArrayList<>();

        if (user == null) {
            return transactionsMap;
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction: bankAccount.getTransactions()) {
                if (TransactionService.TRANSACTION_CATEGORIES.contains(transaction.getCategory())
                        && TransactionType.PAYMENT.equals(transaction.getType())) {

                    transactionsValueDesc.add(transaction);
                }
            }
        }
        transactionsValueDesc.sort((v1, v2) -> v2.getValue().compareTo(v1.getValue()));

        for (Transaction transaction: transactionsValueDesc) {
            transactionsMap.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }
        return transactionsMap;
    }

    /**
     * Вывод последних N транзакций
     * @param user пользователь
     * @param n количество транзакций
     */
    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> lastTransactions = new ArrayList<>();
        if (user == null || n <= 0) {
            return lastTransactions;
        }

        List<Transaction> allTransactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            allTransactions.addAll(bankAccount.getTransactions());
        }

        allTransactions.sort(Comparator.comparing(Transaction::getCreatedDate).reversed());
        for ( int i = 0; i < Math.min(n, allTransactions.size()); i++) {
            lastTransactions.add(allTransactions.get(i));
        }
        return lastTransactions;
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя
     * @param user пользователь
     * @param n количество транзакций
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> topTransactions = new PriorityQueue<>(
                Comparator.comparing(Transaction::getValue).reversed());

        if (user == null || n <= 0) {
            return topTransactions;
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction: bankAccount.getTransactions()) {
                if (transaction.getCategory() != null
                        && TransactionService.TRANSACTION_CATEGORIES.contains(transaction.getCategory())
                        && TransactionType.PAYMENT.equals(transaction.getType())) {
                    if (topTransactions.size() < n) {
                        topTransactions.offer(transaction);
                    } else if (topTransactions.peek() != null
                            && topTransactions.peek().getValue().compareTo(transaction.getValue()) < 0) {
                        topTransactions.poll();
                        topTransactions.offer(transaction);
                    }
                }
            }
        }
        return topTransactions;
    }
}