package week_two;

import week_one.BankAccount;
import week_one.Transaction;
import week_one.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String HEALTH_CATEGORY = "Health";
    private static final String EDUCATION_CATEGORY = "Education";

    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5);
    private static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3);
    private static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1);

    private final TransactionService transactionService = new TransactionService();
    private final AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private final User user = new User("1", "Mark");
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;

    @BeforeEach

    public void setUp() {
        bankAccount1 = new BankAccount("1", user);
        bankAccount2 = new BankAccount("2", user);

        bankAccount1.getTransactions().add(new Transaction("1", TEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        bankAccount1.getTransactions().add(new Transaction("2", FIFTEEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, FIVE_MONTHS_AGO));
        bankAccount2.getTransactions().add(new Transaction("3", TWENTY_DOLLARS, TransactionType.PAYMENT, HEALTH_CATEGORY, THREE_DAYS_AGO));
        bankAccount2.getTransactions().add(new Transaction("4", TWENTY_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, ONE_DAY_AGO));

        user.addAccount(bankAccount1);
        user.addAccount(bankAccount2);
    }

    @Test
    public void get_monthly_spending_by_category() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(TEN_DOLLARS, result);
    }

    @Test
    public void get_monthly_spending_by_category_invalid_input() {
        // Счет равен null
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);

        // Категория равна null
        result = analyticsService.getMonthlySpendingByCategory(bankAccount1, null);
        assertEquals(BigDecimal.ZERO, result);

        // Нет транзакций за последний месяц
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction("5", FIFTEEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, FIVE_MONTHS_AGO));
        result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void get_transaction_history_sorted_by_amount() {
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);
        assertNotNull(result);
        assertEquals(1, result.get(HEALTH_CATEGORY).size());
        assertEquals(2, result.get(BEAUTY_CATEGORY).size());
        assertEquals(1, result.get(EDUCATION_CATEGORY).size());

        assertEquals(TWENTY_DOLLARS, result.get(HEALTH_CATEGORY).get(0).getAmount());
        assertEquals(TWENTY_DOLLARS, result.get(EDUCATION_CATEGORY).get(0).getAmount());
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY).get(0).getAmount());
    }

    @Test
    public void get_transaction_history_sorted_by_amount_invalid_input() {
        // Пользователь равен null
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        user.getAccounts().clear();
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction("6", TEN_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        user.getAccounts().add(bankAccount1);
        result = analyticsService.getTransactionHistorySortedByAmount(user);
        assertTrue(result.isEmpty());
    }

    @Test
    public void get_last_n_transactions() {
        List<Transaction> result = analyticsService.getLastNTransactions(user, 2);
        assertEquals(2, result.size());

        assertEquals("4", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
    }

    @Test
    public void get_last_n_transactions_invalid_input() {
        List<Transaction> result = analyticsService.getLastNTransactions(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций
        user.getAccounts().clear();
        result = analyticsService.getLastNTransactions(user, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void get_top_n_largest_transactions() {
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 2);
        assertEquals(2, result.size());

        Transaction first = result.poll();
        Transaction second = result.poll();

        assertNotNull(first);
        assertEquals(TWENTY_DOLLARS, first.getAmount());
        assertNotNull(second);
        assertEquals(TWENTY_DOLLARS, second.getAmount());
    }

    @Test
    public void get_top_n_largest_transactions_invalid_input() {
        // Пользователь равен null
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        user.getAccounts().clear();
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction("6", TEN_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        user.getAccounts().add(bankAccount1);
        result = analyticsService.getTopNLargestTransactions(user, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMonthlySpendingByCategories() {
        // Пользователь с транзакциями за последний месяц
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(
                user, Set.of(HEALTH_CATEGORY, BEAUTY_CATEGORY, EDUCATION_CATEGORY));

        // Проверка, что есть категория "Beauty", и сумма правильная
        assertTrue(result.containsKey(BEAUTY_CATEGORY));
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY));

        // Проверка, что есть категория "Food", и сумма тоже правильная
        assertTrue(result.containsKey(HEALTH_CATEGORY));
        assertEquals(TWENTY_DOLLARS, result.get(HEALTH_CATEGORY));
    }
}
