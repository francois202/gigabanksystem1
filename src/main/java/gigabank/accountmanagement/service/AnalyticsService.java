package gigabank.accountmanagement.service;

import gigabank.accountmanagement.annotation.LogExecutionTime;
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
     * @param bankAccount - счет
     * @param category - категория
     */
    @LogExecutionTime
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category){
        BigDecimal amount = BigDecimal.ZERO;
        if (bankAccount == null || StringUtils.isBlank(category)) {
            return amount;
        } // Проверка, есть ли транзакции с указанной категорией
        boolean hasCategory = bankAccount.getTransactions().stream()
                .anyMatch(transaction -> category.equals(transaction.getCategory()));
        if (!hasCategory) {
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
     * @param user - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories){
        Map<String, BigDecimal> result = new HashMap<>();
        Set<String> validCategories = new TransactionService().validateCategories(categories);
        if (user == null || validCategories.isEmpty())
            return result;
        LocalDateTime oneMonth = LocalDateTime.now().minusMonths(1L);
        for (BankAccount bankAccount : user.getBankAccounts()){
            for(Transaction transaction : bankAccount.getTransactions()){
                if (TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(oneMonth)){
                    result.merge(transaction.getCategory(), transaction.getValue(), BigDecimal::add);
                }
            }
        }

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
         List<Transaction> transactions = new ArrayList<>();
        for (BankAccount bankAccount : user.getBankAccounts()){
            for (Transaction transaction : bankAccount.getTransactions()){
                if (TransactionType.PAYMENT.equals(transaction.getType()))
                    result.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
//                     transactions.add(transaction);
            }
        }

        transactions.sort(Comparator.comparing(Transaction::getValue));
        for (Transaction transaction : transactions){
            result.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction);
        }

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

        for (BankAccount bankAccount : user.getBankAccounts()){
            allTransaction.addAll(bankAccount.getTransactions());
        }
        allTransaction.sort(Comparator.comparing(Transaction::getCreatedDate).reversed());

        for (int i = 0; i < Math.min(n, allTransaction.size()); i++) {
            result.add(allTransaction.get(i));
        }

        return result;
    }

    /**
     * Вывод топ-N самых больших платежных  транзакций пользователя
     * @param user - пользователь
     * @param n - кол-во последних транзакций
     */
    @LogExecutionTime
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n){
        PriorityQueue<Transaction> result = new PriorityQueue<>(
                Comparator.comparing(Transaction::getValue)
        );
        if (user == null)
            return result;
        for (BankAccount bankAccount : user.getBankAccounts()){
            if (bankAccount != null && bankAccount.getTransactions() != null) {
            for (Transaction transaction : bankAccount.getTransactions()){
                if (TransactionType.PAYMENT.equals(transaction.getType())){
                        result.offer(transaction);
                    if (result.size() > n)  result.poll();
                    }
                }
            }
        }
        return result;
    }
}
