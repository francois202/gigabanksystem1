package gigabank.accountmanagement.service.AnalyzePerformance;

import gigabank.accountmanagement.service.Generate.TransactionTest;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gigabank.accountmanagement.entity.TransactionType.DEPOSIT;
import static gigabank.accountmanagement.entity.TransactionType.PAYMENT;

public class AnalyzePerformance {
    private LocalDateTime minusMonth = LocalDateTime.now().minusMonths(1L);
    /**
     * Анализ выполнения стримов последовательно и параллельно
     */

    public void analyzePerformancesToMap(LinkedList<TransactionTest> transactions) {
        Map<LocalDateTime, List<TransactionTest>> withoutParallel = analyzeWithoutParallel(transactions);
        Map<LocalDateTime, List<TransactionTest>> WithParallel = analyzeWithParallel(transactions);
        System.out.println("--------");
    }

    public void analyzePerformancesToDouble(List<TransactionTest> transactions) {
        analyzeWithoutParallel2(transactions);
        analyzeWithParallel2(transactions);
    }

    private Map<LocalDateTime, List<TransactionTest>> analyzeWithoutParallel(LinkedList<TransactionTest> transactions) {

        long start = System.currentTimeMillis();

        Map<LocalDateTime, List<TransactionTest>> collect = transactions.stream()
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .limit(transactions.size() / 2)
                .collect(Collectors.groupingBy(TransactionTest::getCreatedDate, Collectors.mapping(Function.identity(), Collectors.toList())));

        long end = System.currentTimeMillis();

        long time = end - start;
        System.out.println("Программа была завершена за - " + time + " мс без параллельности выполнения"  + " collect to Map");
        return collect;
    }

    private Map<LocalDateTime, List<TransactionTest>> analyzeWithParallel(LinkedList<TransactionTest> transactions) {

        long start = System.currentTimeMillis();

        Map<LocalDateTime, List<TransactionTest>> collect = transactions.stream()
                .parallel()
                .filter(transaction -> transaction.getType().equals(PAYMENT))
                .filter(transaction -> transaction.getCreatedDate().isAfter(minusMonth))
                .limit(transactions.size() / 2)
                .collect(Collectors.groupingBy(TransactionTest::getCreatedDate, Collectors.mapping(Function.identity(), Collectors.toList())));

        long end = System.currentTimeMillis();


        long time = end - start;
        System.out.println("Программа была завершена за - " + time + "мс с параллельным выполнением" +  " collect to Map");
        return collect;
    }

    private double analyzeWithoutParallel2(List<TransactionTest> transactions) {
        long start = System.currentTimeMillis();

        double result = transactions.stream()
                .filter(transactionTest -> transactionTest.getCreatedDate().isAfter(minusMonth))
                .filter(transactionTest -> transactionTest.getType().equals(DEPOSIT))
                .map(TransactionTest::getValue)
                .mapToInt(value -> value.intValue())
                .average()
                .getAsDouble();

        long end = System.currentTimeMillis();
        long time = end - start;

        System.out.println("Программа была завершена за - " + time + "мс без параллельности выполнения (average value)");
        return result;
    }

    private double analyzeWithParallel2(List<TransactionTest> transactions) {
        long start = System.currentTimeMillis();

        double result = transactions.stream()
                .parallel()
                .filter(transactionTest -> transactionTest.getCreatedDate().isAfter(minusMonth))
                .filter(transactionTest -> transactionTest.getType().equals(DEPOSIT))
                .map(TransactionTest::getValue)
                .mapToInt(value -> value.intValue())
                .average()
                .getAsDouble();

        long end = System.currentTimeMillis();
        long time = end - start;

        System.out.println("Программа была завершена за - " + time + "мс с параллельным выполнением (average value)" );
        return result;
    }
}
