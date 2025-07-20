package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {


    public static Set<String> transactionCategories = Set.of(
            "Food", "Health", "Beauty", "Education");

    public Set<String> validateCategories(Set<String> categories) {
        Set<String> result = new HashSet<>();
        for (String category : categories) {
            if (transactionCategories.contains(category))
                result.add(category);
        }
        return result;
    }

    public static void main(String[] args) {
        List<Transaction> transactions = generateTransactions(10000);
        analyzePerformance(transactions);
    }
    private static List<Transaction> generateTransactions(int count){
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();
        TransactionType[] transactionTypes = TransactionType.values();
        List<String> categories = new ArrayList<>(transactionCategories);


        for (int i = 0; i < count; i++ ){
            String generatedId = UUID.randomUUID().toString();
            BigDecimal value = BigDecimal.valueOf(random.nextDouble()*1000);
            TransactionType type = transactionTypes[random.nextInt(transactionTypes.length)];
            String category = categories.get(random.nextInt(categories.size()));
            LocalDateTime createdDate = LocalDateTime.now().minusDays(random.nextInt(30));
            Transaction transaction = new Transaction(generatedId, value, type, category, createdDate);
            transactions.add(transaction);
        }
        return transactions;
    }

    private static void measureTime(String taskName, Runnable task){
        long startTime = System.nanoTime();
        task.run();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println(taskName + ": " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms");
    }

    private static void analyzePerformance(List<Transaction> transactions){
        System.out.println("Обрабатывается: " + transactions.size() + " транзакций");

        //Фильтрация по типу и подсчёт

        measureTime("Filters and Count (Sequential)", () ->
                transactions.stream()
                        .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                        .count()
        );
        measureTime("Filters and Count (Parallel)", () ->
                transactions.parallelStream()
                        .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                        .count()
        );

        // Сортировка транзакций по сумме и получение первых 10
        measureTime("Sorte and FirstTen(Sequential)",() ->
                transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getValue).reversed())
                        .limit(10)
                        .collect(Collectors.toList())

                );

        measureTime("Sorte and FirstTen(Parallel)",() ->
                transactions.parallelStream()
                        .sorted(Comparator.comparing(Transaction::getValue).reversed())
                        .limit(10)
                        .collect(Collectors.toList())

        );

        // Группировка транзакций по категории и подсчет количества транзакций
        measureTime("GroupCategory and Count(Sequential)", () ->
                transactions.stream()
                        .collect(Collectors.groupingBy(Transaction::getCategory,Collectors.counting()))
        );
        measureTime("GroupCategory and Count(Parallel)", () ->
                transactions.parallelStream()
                        .collect(Collectors.groupingBy(Transaction::getCategory,Collectors.counting()))
        );

        // Вычисление средней суммы транзакций
        measureTime("Calculate Average Value(Sequential)", () ->
                transactions.stream()
                        .mapToDouble(transaction -> transaction.getValue().doubleValue())
                        .average()
                );
        measureTime("Calculate Average Value(Parallel)", () ->
                transactions.parallelStream()
                        .mapToDouble(transaction -> transaction.getValue().doubleValue())
                        .average()
        );






    }


}