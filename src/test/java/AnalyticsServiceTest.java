import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String FOOD_CATEGORY = "Food";
    private static final String EDUCATION_CATEGORY = "Education";

    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5);
    private static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3);
    private static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1);

    private TransactionService transactionService = new TransactionService();
    private AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private User user = new User();
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;

    @BeforeEach
    public void setUp() {
        bankAccount1 = new BankAccount("1", new ArrayList<>());
        bankAccount2 = new BankAccount("2", new ArrayList<>());
        bankAccount1.getTransactions().add(new Transaction(
                "1",
                TEN_DOLLARS,
                TransactionType.PAYMENT,
                BEAUTY_CATEGORY,
                bankAccount1,
                LocalDateTime.now(),
                "Beauty Store",
                "7230",
                "1234-5678-9012-3456",
                "GigaBank",
                null
        ));
        bankAccount1.getTransactions().add(new Transaction(
                "2",
                FIFTEEN_DOLLARS,
                TransactionType.PAYMENT,
                BEAUTY_CATEGORY,
                bankAccount1,
                FIVE_MONTHS_AGO,
                "Spa Center",
                "7230",
                "1234-5678-9012-3456",
                "GigaBank",
                null
        ));
        bankAccount2.getTransactions().add(new Transaction(
                "3",
                TWENTY_DOLLARS,
                TransactionType.PAYMENT,
                FOOD_CATEGORY,
                bankAccount2,
                THREE_DAYS_AGO,
                "Grocery Store",
                "5411",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));
        bankAccount2.getTransactions().add(new Transaction(
                "4",
                TWENTY_DOLLARS,
                TransactionType.PAYMENT,
                EDUCATION_CATEGORY,
                bankAccount2,
                ONE_DAY_AGO,
                "Online Course",
                "8299",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));
        user.setBankAccounts(new ArrayList<>());
        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
    }
    @Test
    @DisplayName("Проверяет ежемесячные расходы по категории Beauty для существующего счета")
    public void get_monthly_spending_by_category() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(TEN_DOLLARS, result);
    }
    @Test
    @DisplayName("Проверяет обработку некорректных входных данных для ежемесячных расходов")
    public void get_monthly_spending_by_category_invalid_input() {
        // Счет равен null
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);

        // Категория равна null
        result = analyticsService.getMonthlySpendingByCategory(bankAccount1, null);
        assertEquals(BigDecimal.ZERO, result);

        // Нет транзакций за последний месяц
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction(
                "4",
                TWENTY_DOLLARS,
                TransactionType.PAYMENT,
                EDUCATION_CATEGORY,
                bankAccount2,
                FIVE_MONTHS_AGO,
                "Online Course",
                "8299",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));
        result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }
    @Test
    @DisplayName("Проверяет историю транзакций пользователя, отсортированных по сумме")
    public void get_transaction_history_sorted_by_amount() {
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);
        assertNotNull(result);
        assertEquals(1, result.get(FOOD_CATEGORY).size());
        assertEquals(2, result.get(BEAUTY_CATEGORY).size());
        assertEquals(1, result.get(EDUCATION_CATEGORY).size());

        assertEquals(TWENTY_DOLLARS, result.get(FOOD_CATEGORY).get(0).getValue());
        assertEquals(TWENTY_DOLLARS, result.get(EDUCATION_CATEGORY).get(0).getValue());
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY).get(0).getValue());
    }
    @Test
    @DisplayName("Проверяет обработку некорректных входных данных для истории транзакций")
    public void get_transaction_history_sorted_by_amount_invalid_input() {
        // Пользователь равен null
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        user.getBankAccounts().clear();
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction(
                "4",
                TWENTY_DOLLARS,
                TransactionType.DEPOSIT,
                BEAUTY_CATEGORY,
                bankAccount2,
                TEN_DAYS_AGO,
                "Online Course",
                "8299",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));
        user.getBankAccounts().add(bankAccount1);
        result = analyticsService.getTransactionHistorySortedByAmount(user);
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("Проверяет получение последних 2 транзакций для пользователя")
    public void get_last_n_transactions() {
        List<Transaction> result = analyticsService.getLastNTransaction(user, 2);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("4", result.get(1).getId());
    }
    @Test
    @DisplayName("Проверяет обработку некорректных входных данных для получения последних транзакций")
    public void get_last_n_transactions_invalid_input() {
        List<Transaction> result = analyticsService.getLastNTransaction(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций
        user.getBankAccounts().clear();
        result = analyticsService.getLastNTransaction(user, 2);
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("Проверяет получение 2 наибольших транзакций для пользователя")
    public void get_top_n_largest_transactions() {
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 2);
        assertEquals(2, result.size());

        Transaction first = result.poll();
        Transaction second = result.poll();

        assertEquals(TWENTY_DOLLARS, first.getValue());
        assertEquals(TWENTY_DOLLARS, second.getValue());
    }
    @Test
    @DisplayName("Проверяет обработку некорректных входных данных для получения наибольших транзакций")
    public void get_top_n_largest_transactions_invalid_input() {
        // Пользователь равен null
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        user.getBankAccounts().clear();
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction(
                "6",
                TEN_DOLLARS,
                TransactionType.DEPOSIT,
                BEAUTY_CATEGORY,
                bankAccount2,
                TEN_DAYS_AGO,
                "Online Course",
                "8299",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));
        user.getBankAccounts().add(bankAccount1);
        result = analyticsService.getTopNLargestTransactions(user, 2);
        assertTrue(result.isEmpty());
    }
    @Test
    @DisplayName("Сравнивает производительность последовательного и параллельного стримов")
    public void analyze_performance() {

        List<Transaction> transactions = user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .collect(Collectors.toList());

        long startTime = System.currentTimeMillis();
        transactions.stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("1000")) > 0)
                .sorted(Comparator.comparing(Transaction::getValue))
                .count();
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential stream time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        transactions.parallelStream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("1000")) > 0)
                .sorted(Comparator.comparing(Transaction::getValue))
                .count();
        endTime = System.currentTimeMillis();
        System.out.println("Parallel stream time: " + (endTime - startTime) + " ms");
    }
}