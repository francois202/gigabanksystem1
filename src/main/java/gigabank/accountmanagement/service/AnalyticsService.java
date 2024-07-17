package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
public class AnalyticsService {
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     *
     * @param bankAccount - счет
     * @param category    - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category) {
        BigDecimal sum = BigDecimal.ZERO;
        if (bankAccount == null || category == null
                || !TransactionService.TRANSACTION_CATEGORIES.contains(category)) {
            return sum;
        }

        LocalDateTime lastMonth = LocalDateTime.now().minus(1L, ChronoUnit.MONTHS);

        sum = bankAccount.getTransactions().stream()
                .filter(t -> t.getCreatedDate().isAfter(lastMonth))
                .filter(t -> StringUtils.equals(t.getCategory(), category))
                .filter(t -> TransactionType.PAYMENT.equals(t.getType()))
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        Map<String, BigDecimal> monthlySpendingMap = new HashMap<>();

        if (user == null || categories == null) {
            return monthlySpendingMap;
        }

        Set<String> validatedCategories = categories.stream()
                .filter(c -> TransactionService.TRANSACTION_CATEGORIES.contains(c))
                .collect(Collectors.toCollection(HashSet::new));

        if (validatedCategories.isEmpty()) {
            return monthlySpendingMap;
        }

        LocalDateTime lastMonth = LocalDateTime.now().minus(1L, ChronoUnit.MONTHS);

        monthlySpendingMap = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .filter(t -> t.getCreatedDate().isAfter(lastMonth))
                .filter(t -> TransactionType.PAYMENT.equals(t.getType()))
                .filter(t -> TransactionService.TRANSACTION_CATEGORIES.contains(t.getCategory()))
                .collect(Collectors.toMap(Transaction::getCategory, Transaction::getValue, BigDecimal::add));

        return monthlySpendingMap;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     *
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public LinkedHashMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user) {
        LinkedHashMap<String, List<Transaction>> transactionsMap = new LinkedHashMap<>();

        if (user == null) {
            return transactionsMap;
        }

        transactionsMap = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .filter(t -> TransactionType.PAYMENT.equals(t.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .collect(Collectors.groupingBy(Transaction::getCategory, LinkedHashMap::new, Collectors.toList()));

        return transactionsMap;
    }

    /**
     * Вывод последних N транзакций
     *
     * @param user пользователь
     * @param n    количество транзакций
     */
    public List<Transaction> getLastNTransactions(User user, int n) {
        List<Transaction> lastTransactions = new ArrayList<>();
        if (user == null || n <= 0) {
            return lastTransactions;
        }

        lastTransactions = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .sorted(Comparator.comparing(Transaction::getCreatedDate).reversed())
                .limit(n)
                .collect(Collectors.toList());

        return lastTransactions;
    }

    /**
     * Вывод топ-N самых больших платежных транзакций пользователя
     *
     * @param user пользователь
     * @param n    количество транзакций
     */
    public PriorityQueue<Transaction> getTopNLargestTransactions(User user, int n) {
        PriorityQueue<Transaction> topTransactions = new PriorityQueue<>();

        if (user == null || n <= 0) {
            return topTransactions;
        }

        topTransactions = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .filter(t -> TransactionType.PAYMENT.equals(t.getType()))
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .limit(n)
                .collect(Collectors.toCollection(() -> new PriorityQueue<>(
                        Comparator.comparing(Transaction::getValue).reversed())));

        return topTransactions;
    }

    /*-------------------------------------------------------*/

    /**
     * Метод для замера времени выполнения для обычных и параллельных стримов
     *
     * @param transactions список транзакци
     * @return возвращает результаты в виде списка
     */
    public List<String> analyzePerformance(List<Transaction> transactions) {
        List<String> totalResult = new ArrayList<>();
        if (transactions == null) {
            return totalResult;
        }

        //----Вывод транзакций c платежом больше $1000----
        String resultTimeStream = "";
        LocalTime before = LocalTime.now();
        List<Transaction> transactionsFilteredAmount = new ArrayList<>(transactions.stream()
                .filter(t -> t.getValue().compareTo(new BigDecimal("1000.00")) > 0)
                .toList());

        Duration delta = Duration.between(before, LocalTime.now());

        resultTimeStream = "Фильтруем " + (transactions.size() - 1) +
                " транзакций и выводим c платежом больше $1000...\n" +
                "Получено " + (transactionsFilteredAmount.size() - 1) + " транзакций |" +
                " Время выполнения stream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeStream);

        String resultTimeParallelStream = "";
        before = LocalTime.now();
        List<Transaction> transactionsFilteredAmountPar = new ArrayList<>(transactions.parallelStream()
                .filter(t -> t.getValue().compareTo(new BigDecimal("1000.00")) > 0)
                .toList());

        delta = Duration.between(before, LocalTime.now());

        resultTimeParallelStream = "Получено " + (transactionsFilteredAmountPar.size() - 1) + " транзакций |" +
                " Время выполнения parallelStream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeParallelStream);


        //---Сортировка транзакций по сумме платежа от наибольшей к меньшей---
        before = LocalTime.now();
        List<Transaction> transactionsSortedAmount = new ArrayList<>(transactions.stream()
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .toList());
        delta = Duration.between(before, LocalTime.now());

        resultTimeStream = "--------------------------------------\n" +
                "Сортируем " + (transactions.size() - 1) +
                " транзакций по сумме платежа...\n" +
                "Осортировано " + (transactionsSortedAmount.size() - 1) + " транзакций |" +
                " Время выполнения stream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeStream);

        transactionsSortedAmount.clear();
        before = LocalTime.now();
        transactionsSortedAmount = new ArrayList<>(transactions.parallelStream()
                .sorted(Comparator.comparing(Transaction::getValue).reversed())
                .toList());

        delta = Duration.between(before, LocalTime.now());

        resultTimeParallelStream = "Осортировано " + (transactionsSortedAmount.size() - 1) + " транзакций |" +
                " Время выполнения parallelStream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeParallelStream);


        //-------Вывод суммы всех транзакций --------
        before = LocalTime.now();
        BigDecimal transactionsSum = transactions.stream()
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        delta = Duration.between(before, LocalTime.now());

        resultTimeStream = "--------------------------------------\n" +
                "Суммируем " + (transactions.size() - 1) + " транзакций...\n" +
                "Сумма: " + transactionsSum + " | Время выполнения stream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeStream);

        transactionsSum = BigDecimal.ZERO;
        before = LocalTime.now();
        transactionsSum = transactions.parallelStream()
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        delta = Duration.between(before, LocalTime.now());

        resultTimeParallelStream = "Сумма: " + transactionsSum +
                " | Время выполнения parallelStream(): " + delta.toMillis() + " мс";
        totalResult.add(resultTimeParallelStream);

        return totalResult;
    }
    /*
     * Результаты работы метода analyzePerformance для теста операций по значениям сумм транзакций:
     * Время работы обычного и параллельного стримов обозначено как - "220:180 мс"
     * 220 - обычный, 180 - параллельный
     * ===================================================================
     * Количество транзакций -> |  100 000  |  1 000 000  |  10 000 000
     * -------------------------------------------------------------------
     * Фильтрация ArrayList     |  14:10 мс |   69:46 мс  |  698:232 мс
     * Фильтрация LinkedList    |  20:26 мс |  220:180 мс | 1165:2003 мс
     * -------------------------------------------------------------------
     * Сортировка ArrayList     |  86:68 мс |  659:272 мс | 6371:1619 мс
     * Сортировка LinkedList    |  94:76 мс |  798:409 мс | 9202:3930 мс
     * -------------------------------------------------------------------
     * Сумма ArrayList          |   6:9 мс  |   46:24 мс  |  386:131 мс
     * Сумма LinkedList         |   7:9 мс  |   56:201 мс | 2417:867 мс
     * ===================================================================
     * Выводы:
     * 1) Параллельный стрим на длинной дистанции показывает себя более
     * производительным.
     * 2) Наиболее затратная операция это сортировка, так как происходит перебор всех элементов.
     * 3) LinkedList проигрывает по всем показателям в сравнении с ArrayList.
     * 4) При нахождении суммы всех элементов паралелльный стрим у LinkedList,
     *    сильно проигрывая на короткой и средней дистанции, кардинально меняет ситуацию
     *    на длинной дистанции и вырывается далеко вперёд по производительности.
     * */
}