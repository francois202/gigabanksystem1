package org.example;

import BankAcount.src.BankAccount;
import BankAcount.src.Transaction;
import BankAcount.src.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static javax.swing.UIManager.put;

public class AnalyticsService {
    private final TransactionService transactionService;

    public AnalyticsService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal totalSum = BigDecimal.ZERO; //создали переменную сумму и назначили ей нуль
        if (bankAccount == null || !TransactionService.isValidTransactionCategory(category)) {
            return totalSum;
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1L); // создал переменную в которой вычитается 1 месяц от реального времени
        for (Transaction transaction : bankAccount.getTransactions()) { // создаю цикл, где создается переменная transaction класса Transaction и наш банковский акк из параметров вызывает метод гет транзакшинс
            if (TransactionType.PAYMENT.equals(transaction.getType())  // транзакция вызывает метод и проверяется что тип совпадает с PAYMENT из enum(перечисления)
                    && transaction.getCreatedDate().isAfter(oneMonthAgo)) { // проверяем что транзакция сделана после переменной oneMonthAgo
                totalSum = totalSum.add(transaction.getValue());
            }
        }
        return totalSum;
    }

    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories) {
        Map<String, BigDecimal> map = new HashMap<>(); // создали мапу для результатов
        Set<String> validCategories = transactionService.validateTransactionCategories(categories); // создали сет для валидных категорий
        if (user != null || validCategories.isEmpty()) {  //проверка что пользователь не нуль и что сет валидных категорий не пустой
            return map;
        }
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1L);
        for (BankAccount bankAccount : user.getAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())
                        && validCategories.contains(transaction.getCategory())
                        && transaction.getCreatedDate().isAfter(oneMonthAgo)) {

                    var transactionCategory = transaction.getCategory();
                    if (!map.containsKey(transaction.getCategory())) {
                        map.put(transaction.getCategory(), transaction.getValue());
                    } else {
                        var categoryValue = map.get(transaction.getCategory());
                        categoryValue = categoryValue.add(transaction.getValue());
                        map.put(transaction.getCategory(), categoryValue);
                    }
                }
            }
        }
        return null;
    }

    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> map = new LinkedHashMap<>();
        List<Transaction> transactions = new ArrayList<>();
        if (user == null) {
            return map;
        }
        for (BankAccount bankAccount : user.getAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    transactions.add(transaction);
                }
            }
        }
        transactions.sort(Comparator.comparing(Transaction::getValue));
        for (Transaction transaction : transactions) {
            map.computeIfAbsent(transaction.getCategory(), k -> new ArrayList<>()).add(transaction); //метод проверяет существовать ли такая категория
            //если да то "k -> new ArrayList<>()" эта часть кода пропускается и просто добавляется транзакция, а если такой категории нет, то создается еще новый массив для этой категории
        }
        return map;
    }

    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> transactions = new ArrayList<>();
        if (user == null || transactions.isEmpty()) {
            return transactions;
        }
        for (BankAccount bankAccount : user.getAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                transactions.add(transaction);
                transactions.sort(Comparator.comparing(Transaction::getCreatedDate).reversed());
                return transactions.subList(0, Math.min(n, transactions.size())); // sublist возвращает от 0 до Math.min, в нашем случае либо n либо размер списка транзакций
            }
        }

        return transactions;
    }

    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> transactions =
                new PriorityQueue<>(Comparator.comparing(Transaction::getValue));
        if (user == null) {
            return transactions;
        }
        for (BankAccount bankAccount : user.getAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (TransactionType.PAYMENT.equals(transaction.getType())) {
                    if (transactions.size() < n) {
                        transactions.offer(transaction);
                    } else if (transactions.peek().getValue().compareTo(transaction.getValue()) < 0) { // вызывается элемент для просмотра из тех что уже добавлены в очередь
                        // и сравниваются с другими элементами, возвращается отрицательное число (сравнение compareTo)
                        transactions.poll();
                        transactions.offer(transaction);
                    }
                }
            }
        }

        return transactions;
    }

}



