package gigabank.accountmanagement.service;

import gigabank.accountmanagement.annotation.LogExecutionTime;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

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
     * @param bankAccount - счет
     * @param category    - категория
     */
    @LogExecutionTime
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal amount = BigDecimal.ZERO;
        if (bankAccount == null || StringUtils.isBlank(category)) {
            return amount;
        }
        LocalDateTime oneMonth = LocalDateTime.now().minusMonths(1L);
        for (Transaction transaction : bankAccount.getTransactions()) {
            if (TransactionType.PAYMENT.equals(transaction.getType())
                    && transaction.getCategory().equals(category)
                    && transaction.getCreatedDate().isAfter(oneMonth)) {
                amount = amount.add(transaction.getValue());
            }
        }
        return amount;
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
        Set<String> validCategories = transactionService.validateCategories(categories);
        if (user == null || validCategories.isEmpty())
            return result;
        LocalDateTime oneMonth = LocalDateTime.now().minusMonths(1L);
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(oneMonth)) {
                    result.merge(transaction.getCategory(), transaction.getValue(), BigDecimal::add);
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
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> categorizedTransactions = new LinkedHashMap<>();
        if (user == null) {
            return categorizedTransactions;
        }
        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    categorizedTransactions.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
                }
            }
        }
        for (List<Transaction> transactions : categorizedTransactions.values()) {
            transactions.sort(Comparator.comparing(Transaction::getValue));
        }
        return categorizedTransactions;
    }

    /**
     * Вывод последних N транзакций пользователя
     *
     * @param user - пользователь
     * @param n    - кол-во последних транзакций
     */
    public List<Transaction> getLastNTransaction(User user, int n) {
        if (user == null || n <= 0) {
            return List.of();
        }
        List<Transaction> allTransactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()) {
            allTransactions.addAll(bankAccount.getTransactions());
        }
        allTransactions.sort(Comparator.comparing(Transaction::getCreatedDate).reversed());
        return List.copyOf(allTransactions.subList(0, Math.min(n, allTransactions.size())));
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя
     *
     * @param user - пользователь
     * @param n    - кол-во последних транзакций
     */
    @LogExecutionTime
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(
                Comparator.comparing(Transaction::getValue)
        );
        if (user == null)
            return result;
        for (BankAccount bankAccount : user.getBankAccounts()) {
            if (bankAccount != null && bankAccount.getTransactions() != null) {
                for (Transaction transaction : bankAccount.getTransactions()) {
                    if (TransactionType.PAYMENT.equals(transaction.getType())) {
                        result.offer(transaction);
                        while (result.size() > n) {
                            result.poll();
                        }
                    }
                }
            }
        }
        return result;
    }
}