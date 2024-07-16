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

        for (Transaction transaction : bankAccount.getTransactions()) {
            if (TransactionType.PAYMENT.equals(transaction.getType())
                    && StringUtils.equals(transaction.getCategory(), category)
                    && transaction.getCreatedDate().isAfter(ONE_MONTH)) {
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
        Set<String> validCategories = TRANSACTION_SERVICE.validateCategories(categories);
        if (user == null || validCategories.isEmpty())
            return result;

        for (BankAccount bankAccount : user.getBankAccounts()){
            for(Transaction transaction : bankAccount.getTransactions()){
                if (TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(ONE_MONTH)){
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
                    // transactions.add(transaction);
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
        // .sort((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate())); --> так не хочет работать не видит .getCreatedDate()
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
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n){
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(Transaction::getValue));

        if (user == null)
            return result;


        for (BankAccount bankAccount : user.getBankAccounts()){
            for (Transaction transaction : bankAccount.getTransactions()){
                if (TransactionType.PAYMENT.equals(transaction.getType())){
                    if (result.size() < n)
                        result.offer(transaction);
                    else if (result.peek() != null
                            && result.peek().getValue().compareTo(transaction.getValue()) < 0){
                        result.poll();
                        result.offer(transaction);
                    }
                }
            }
        }

        return result;
    }


}
