package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     * @param bankAccount - счет
     * @param category - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category){
        BigDecimal amount = BigDecimal.ZERO;
        if (bankAccount == null)
            return amount;
        LocalDateTime oneMonth = LocalDateTime.now().minusMonths(1L);
        for (Transaction transaction : bankAccount.getTransactions()){
            bankAccount.getTransactions().stream()
                    .filter(transactions -> TransactionType.PAYMENT.equals(transaction.getType()))
                    .filter(transactions -> transactions.getCategory().equals(category))
                    .filter(transactions -> transactions.getCreatedDate().isAfter(oneMonth))
                    .map(Transaction::getValue) //Преобразует поток транзакций в поток значений транзакций (BigDecimal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    public Map<String, BigDecimal> getMonthlySpendingByCategories (User user, Set<String> categories) {
        Map<String, BigDecimal> result = new HashMap<>();
        Set<String> validCategories = new TransactionService().validateCategories(categories);
        if (user == null || validCategories.isEmpty())
            return result;
        LocalDateTime oneMonth = LocalDateTime.now().minusMonths(1L);

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transactions-> TransactionType.PAYMENT.equals(transactions.getType()))
                .filter(transactions -> validCategories.contains(transactions.getCategory()))
                .filter(transactions -> transactions.getCreatedDate().isAfter(oneMonth))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                         Collectors.reducing(
                                 BigDecimal.ZERO,
                                 Transaction::getValue,
                                 BigDecimal::add)));

    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction> > getTransactionHistorySortedByAmount(User user){
        LinkedHashMap<String, List<Transaction>> result = new LinkedHashMap<>();
        if (user == null)
            return result;
        List<Transaction> transactions = new ArrayList<>();

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction-> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue))
                .collect(Collectors.groupingBy(Transaction::getCategory,
                                               LinkedHashMap::new,
                                               Collectors.toList()));

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
        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }
    /**
     * Вывод топ-N самых больших платежных транзакций пользователя
     * @param user - пользователь
     * @param n - кол-во последних транзакций
     */

    public PriorityQueue<Transaction> getTopLargestTransactions(User user, int n){
        PriorityQueue<Transaction> result = new PriorityQueue<>();

        if (user == null)
            return result;
        List<Transaction> allTransaction = new ArrayList<>();

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection(
                        ()-> new PriorityQueue<>(Comparator.comparing(Transaction::getValue).reversed())));

    }


}

