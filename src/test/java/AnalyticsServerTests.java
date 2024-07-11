import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private static final BigDecimal AMOUNT_10 = new BigDecimal("10.00");
    private static final BigDecimal AMOUNT_15 = new BigDecimal("15.00");
    private static final BigDecimal AMOUNT_20 = new BigDecimal("20.00");

    private static final String CATEGORY_BEAUTY = "Beauty";
    private static final String CATEGORY_FOOD = "Food";
    private static final String CATEGORY_EDUCATION = "Education";

    private static final LocalDateTime DATE_10_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime DATE_5_MONTHS_AGO = LocalDateTime.now().minusMonths(5);
    private static final LocalDateTime DATE_3_DAYS_AGO = LocalDateTime.now().minusDays(3);
    private static final LocalDateTime DATE_1_DAY_AGO = LocalDateTime.now().minusDays(1);

    private TransactionService transactionService = new TransactionService();
    private AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private User testUser = new User();
    private BankAccount account1;
    private BankAccount account2;

    @BeforeEach
    public void init() {
        account1 = new BankAccount();
        account2 = new BankAccount();

        account1.getTransactions().add(new Transaction("1", AMOUNT_10, TransactionType.PAYMENT, CATEGORY_BEAUTY, DATE_10_DAYS_AGO));
        account1.getTransactions().add(new Transaction("2", AMOUNT_15, TransactionType.PAYMENT, CATEGORY_BEAUTY, DATE_5_MONTHS_AGO));
        account2.getTransactions().add(new Transaction("3", AMOUNT_20, TransactionType.PAYMENT, CATEGORY_FOOD, DATE_3_DAYS_AGO));
        account2.getTransactions().add(new Transaction("4", AMOUNT_20, TransactionType.PAYMENT, CATEGORY_EDUCATION, DATE_1_DAY_AGO));

        testUser.getBankAccounts().add(account1);
        testUser.getBankAccounts().add(account2);
    }

    @Test
    public void testGetMonthlySpendingByCategory() {
        BigDecimal spending = analyticsService.getMonthlySpendingByCategory(account1, CATEGORY_BEAUTY);
        assertEquals(AMOUNT_10, spending);
    }

    @Test
    public void testGetMonthlySpendingByCategoryWithInvalidInput() {
        // Account is null
        BigDecimal spending = analyticsService.getMonthlySpendingByCategory(null, CATEGORY_BEAUTY);
        assertEquals(BigDecimal.ZERO, spending);

        // Category is null
        spending = analyticsService.getMonthlySpendingByCategory(account1, null);
        assertEquals(BigDecimal.ZERO, spending);

        // No transactions in the last month
        account1.getTransactions().clear();
        account1.getTransactions().add(new Transaction("5", AMOUNT_15, TransactionType.PAYMENT, CATEGORY_BEAUTY, DATE_5_MONTHS_AGO));
        spending = analyticsService.getMonthlySpendingByCategory(account1, CATEGORY_BEAUTY);
        assertEquals(BigDecimal.ZERO, spending);
    }

    @Test
    public void testGetMonthlySpendingByCategories() {
        Set<String> categories = new HashSet<>(Arrays.asList(CATEGORY_BEAUTY, CATEGORY_FOOD));
        Map<String, BigDecimal> spending = analyticsService.getMonthlySpendingByCategories(testUser, categories);

        assertEquals(AMOUNT_10, spending.get(CATEGORY_BEAUTY));
        assertEquals(AMOUNT_20, spending.get(CATEGORY_FOOD));
        assertNull(spending.get(CATEGORY_EDUCATION));
    }

    @Test
    public void testGetMonthlySpendingByCategoriesWithInvalidInput() {
        Set<String> categories = new HashSet<>(Arrays.asList(CATEGORY_BEAUTY, CATEGORY_FOOD));

        // User is null
        Map<String, BigDecimal> spending = analyticsService.getMonthlySpendingByCategories(null, categories);
        assertTrue(spending.isEmpty());

        // No transactions in the last month
        testUser.getBankAccounts().clear();
        account1.getTransactions().clear();
        account1.getTransactions().add(new Transaction("5", AMOUNT_15, TransactionType.PAYMENT, CATEGORY_BEAUTY, DATE_5_MONTHS_AGO));
        testUser.getBankAccounts().add(account1);
        spending = analyticsService.getMonthlySpendingByCategories(testUser, categories);
        assertTrue(spending.isEmpty());
    }

    @Test
    public void testGetTransactionHistorySortedByAmount() {
        TreeMap<String, List<Transaction>> history = analyticsService.getTransactionHistorySortedByAmount(testUser);
        assertNotNull(history);
        assertEquals(1, history.get(CATEGORY_FOOD).size());
        assertEquals(2, history.get(CATEGORY_BEAUTY).size());
        assertEquals(1, history.get(CATEGORY_EDUCATION).size());

        assertEquals(AMOUNT_20, history.get(CATEGORY_FOOD).get(0).getValue());
        assertEquals(AMOUNT_20, history.get(CATEGORY_EDUCATION).get(0).getValue());
        assertEquals(AMOUNT_10, history.get(CATEGORY_BEAUTY).get(0).getValue());
    }

    @Test
    public void testGetTransactionHistorySortedByAmountWithInvalidInput() {
        // User is null
        TreeMap<String, List<Transaction>> history = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(history.isEmpty());

        // No PAYMENT transactions
        testUser.getBankAccounts().clear();
        account1.getTransactions().clear();
        account1.getTransactions().add(new Transaction("6", AMOUNT_10, TransactionType.DEPOSIT, CATEGORY_BEAUTY, DATE_10_DAYS_AGO));
        testUser.getBankAccounts().add(account1);
        history = analyticsService.getTransactionHistorySortedByAmount(testUser);
        assertTrue(history.isEmpty());
    }

    @Test
    public void testGetLastNTransactions() {
        List<Transaction> transactions = analyticsService.getLastNTransactions(testUser, 2);
        assertEquals(2, transactions.size());

        assertEquals("4", transactions.get(0).getId());
        assertEquals("3", transactions.get(1).getId());
    }

    @Test
    public void testGetLastNTransactionsWithInvalidInput() {
        List<Transaction> transactions = analyticsService.getLastNTransactions(null, 2);
        assertTrue(transactions.isEmpty());

        // No transactions
        testUser.getBankAccounts().clear();
        transactions = analyticsService.getLastNTransactions(testUser, 2);
        assertTrue(transactions.isEmpty());
    }

    @Test
    public void testGetTopNLargestTransactions() {
        PriorityQueue<Transaction> largestTransactions = analyticsService.getTopNLargestTransactions(testUser, 2);
        assertEquals(2, largestTransactions.size());

        Transaction first = largestTransactions.poll();
        Transaction second = largestTransactions.poll();

        assertEquals(AMOUNT_20, first.getValue());
        assertEquals(AMOUNT_20, second.getValue());
    }

    @Test
    public void testGetTopNLargestTransactionsWithInvalidInput() {
        // User is null
        PriorityQueue<Transaction> largestTransactions = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(largestTransactions.isEmpty());

        // No PAYMENT transactions
        testUser.getBankAccounts().clear();
        account1.getTransactions().clear();
        account1.getTransactions().add(new Transaction("6", AMOUNT_10, TransactionType.DEPOSIT, CATEGORY_BEAUTY, DATE_10_DAYS_AGO));
        testUser.getBankAccounts().add(account1);
        largestTransactions = analyticsService.getTopNLargestTransactions(testUser, 2);
        assertTrue(largestTransactions.isEmpty());
    }
}