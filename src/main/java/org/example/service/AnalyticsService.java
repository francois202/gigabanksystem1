package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.BankAccount;
import org.example.entity.Transaction;
import org.example.entity.TransactionType;
import org.example.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
        BigDecimal sum = BigDecimal.ZERO;

        if (bankAccount == null || !transactionService.isValidCategory(category))
            return sum;

        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1L);
        for (Transaction transaction : bankAccount.getTransactions()) {
            if (transaction.getCategory().equals(category) &&
                    transaction.getCreatedDate().isAfter(lastMonth) &&
                    transaction.getType().equals(TransactionType.PAYMENT)) {
                sum = sum.add(transaction.getValue());
            }
        }

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
        Map<String, BigDecimal> result = new HashMap<>();

        if (user == null)
            return result;

        for (String category : categories) {
            for (BankAccount bankAccount : user.getBankAccounts()) {
                BigDecimal monthlyExpensesByCategory = getMonthlySpendingByCategory(bankAccount, category);
                // getMonthlySpendingByCategory возвращает BigDecimal.ZERO => значение не валидное
                if (!monthlyExpensesByCategory.equals(BigDecimal.ZERO))
                    result.merge(category, monthlyExpensesByCategory, BigDecimal::add);
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
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> result = new LinkedHashMap<>();
        if (user == null)
            return result;

        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType()))
                    transactions.add(transaction);
            }
        }

        transactions.sort((t1, t2) -> t2.getValue().compareTo(t1.getValue()));
        for (Transaction transaction : transactions) {
            result.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }

        return result;
    }

    /**
     * Вывод последних N транзакций пользователя
     *
     * @param user - пользователь
     * @param n    - кол-во последних транзакций
     */
    public List<Transaction> getLastNTransaction(User user, int n) {
        List<Transaction> result = new ArrayList<>();

        if (user == null)
            return result;

        List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            transactions.addAll(bankAccount.getTransactions());
        }

        transactions.sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()));

        for (int i = 0; i < Math.min(n, transactions.size()); i++) {
            result.add(transactions.get(i));
        }

        return result;
    }

    /**
     * Вывод топ-N самых больших платежных  транзакций пользователя
     *
     * @param user - пользователь
     * @param n    - кол-во последних транзакций
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(Transaction::getValue));

        if (user == null)
            return result;

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    if (result.size() < n)
                        result.offer(transaction);
                    else if (result.peek() != null
                            && result.peek().getValue().compareTo(transaction.getValue()) < 0) {
                        result.poll();
                        result.offer(transaction);
                    }
                }
            }
        }

        return result;
    }


}
