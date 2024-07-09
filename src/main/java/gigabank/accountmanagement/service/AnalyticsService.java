package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.w3c.dom.ls.LSOutput;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

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

        for (Transaction transaction : bankAccount.getTransactions()) {

            if (transaction.getCategory() != category) {
                continue;
            }

            if (transaction.getCategory().equals(category)
                && transaction.getType().equals(PAYMENT)
                && transaction.getCreatedDate().isAfter(minusMonth)) {

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
        Map<String, BigDecimal> categorySum = new HashMap<>();

        if (user == null) {
            return new HashMap<>();
        }

        for (String category : categories) {
            categorySum.put(category, BigDecimal.ZERO);
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getType() == PAYMENT &&
                    transaction.getCreatedDate().isAfter(minusMonth)) {

                    String category = transaction.getCategory();

                    if (categories.contains(category)) {
                        BigDecimal currentSum = categorySum.get(category);
                        BigDecimal updateSum = currentSum.add(transaction.getValue());

                        categorySum.put(category, updateSum);
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }

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
        List<Transaction> transactionList = new ArrayList<>();

        if (user == null) {
            return result;
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getType() == PAYMENT) {
                    transactionList.add(transaction);
                }
            }
        }
        transactionList.sort(Comparator.comparing(Transaction::getValue).reversed());


        for (Transaction transaction : transactionList) {
            result.computeIfAbsent(transaction.getCategory(), key -> new ArrayList<>()).add(transaction);
        }


        return result;
    }

    public LinkedHashMap<LocalDateTime, Transaction> getTransactionListByIdentification(User user, int num) {
        LinkedHashMap<LocalDateTime, Transaction> result = new LinkedHashMap<>();
        List<Transaction> transactionTime = new ArrayList<>();

        if (user == null) {
            return result;
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                transactionTime.add(transaction);
            }
        }

        transactionTime.sort(Comparator.comparing(transaction -> transaction.getCreatedDate().getDayOfMonth(),
                Comparator.reverseOrder()));


        for (int i = 0; i < Math.min(num, transactionTime.size()); i++) {
            result.put(transactionTime.get(i).getCreatedDate(), transactionTime.get(i));
        }


        return result;
    }

    public PriorityQueue<Transaction> getLargestUserTransaction(User user, int num) {
        PriorityQueue<Transaction> result = new PriorityQueue<>(Comparator.comparing(transaction -> transaction.getValue(),
                Comparator.reverseOrder()));
        List<Transaction> transactionsList = new ArrayList<>();

        if (user == null) {
            return result;
        }

        for (BankAccount bankAccount : user.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (!bankAccount.getTransactions().isEmpty() && bankAccount.getTransactions().size() >= num) {
                    if (transaction.getType() == PAYMENT) {
                        transactionsList.add(transaction);
                    } else {
                        continue;
                    }
                } else {
                    return result;
                }
            }
        }


        transactionsList.sort(Comparator.comparing(Transaction::getValue).reversed());


        for (int i = 0; i < Math.min(num, transactionsList.size()); i++) {
            result.add(transactionsList.get(i));
        }


        return result;
    }

    public Boolean createNewBankAccountForUser(User user, BankAccount bankAccount) {
        if (bankAccount != null && user != null) {
            user.getBankAccounts().add(bankAccount);
            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteUserBankAccount(User user, BankAccount bankAccount) {
        if (user == null) {
            return false;
        }
        if (user.getBankAccounts().contains(bankAccount)) {
            user.getBankAccounts().remove(bankAccount);
            return true;
        } else {
            throw new IllegalArgumentException("Bank account does not exist");
        }
    }

    public Boolean replenishmentBankAccount(BankAccount bankAccount, BigDecimal sum, Transaction transaction) {

        if (bankAccount == null || transaction == null){
            return false;
        }

        BigDecimal balance = bankAccount.getBalance();


        if (sum.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(sum);
            bankAccount.getTransactions().add(transaction);
            return true;
        } else {
            return false;
        }
    }

    public Boolean paymentFromBankAccount(BankAccount bankAccount, Transaction transaction) {
        List<Transaction> transactions = bankAccount.getTransactions();

        if (bankAccount == null || transactions == null) {
            return false;
        }

        if (bankAccount.getBalance().compareTo(transaction.getValue()) >= 0) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getValue()));
            transactions.add(transaction);
            return true;
        } else {
            return false;
        }
    }

    public Boolean paymentFromAndToAccount(BankAccount fromAccount, BankAccount toAccount, BigDecimal sum) {

        if(fromAccount == null || toAccount == null || sum.compareTo(BigDecimal.ZERO) < 0){
            return false;
        }
        if (fromAccount.getBalance().compareTo(sum) >= 0) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(sum));
            toAccount.setBalance(toAccount.getBalance().add(sum));

            Transaction transactionFrom = new Transaction("1", sum, TRANSFER, "Transfer", LocalDateTime.now());
            Transaction transactionTo = new Transaction("2", sum, TRANSFER, "Transfer", LocalDateTime.now());

            fromAccount.getTransactions().add(transactionFrom);
            toAccount.getTransactions().add(transactionTo);
            return true;
        }else{
            return false;
        }
    }
}
