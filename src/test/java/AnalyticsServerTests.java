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
    private AnalyticsService analyticsService = new AnalyticsService();
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
        BigDecimal spending = analyticsService.getMonthlySpendingByCategory(null, CATEGORY_BEAUTY);
        assertEquals(BigDecimal.ZERO, spending);

        spending = analyticsService.getMonthlySpendingByCategory(account1, null);
        assertEquals(BigDecimal.ZERO, spending);

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

        Map<String, BigDecimal> spending = analyticsService.getMonthlySpendingByCategories(null, categories);
        assertTrue(spending.isEmpty());

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

        TreeMap<String, List<Transaction>> history = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(history.isEmpty());

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

        PriorityQueue<Transaction> largestTransactions = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(largestTransactions.isEmpty());

        testUser.getBankAccounts().clear();
        account1.getTransactions().clear();
        account1.getTransactions().add(new Transaction("6", AMOUNT_10, TransactionType.DEPOSIT, CATEGORY_BEAUTY, DATE_10_DAYS_AGO));
        testUser.getBankAccounts().add(account1);
        largestTransactions = analyticsService.getTopNLargestTransactions(testUser, 2);
        assertTrue(largestTransactions.isEmpty());
    }

    @Test
    public void testAnalyzePerformance() {
        Transaction transaction1 = new Transaction();
        transaction1.setType(TransactionType.PAYMENT);
        transaction1.setCategory("Food");
        transaction1.setValue(new BigDecimal("50.00"));
        transaction1.setCreatedDate(LocalDateTime.now().minusDays(10));

        Transaction transaction2 = new Transaction();
        transaction2.setType(TransactionType.PAYMENT);
        transaction2.setCategory("Utilities");
        transaction2.setValue(new BigDecimal("150.00"));
        transaction2.setCreatedDate(LocalDateTime.now().minusDays(20));

        Transaction transaction3 = new Transaction();
        transaction3.setType(TransactionType.PAYMENT);
        transaction3.setCategory("Food");
        transaction3.setValue(new BigDecimal("30.00"));
        transaction3.setCreatedDate(LocalDateTime.now().minusDays(5));

        BankAccount bankAccount1 = new BankAccount();
        bankAccount1.setTransactions(Arrays.asList(transaction1, transaction2));

        BankAccount bankAccount2 = new BankAccount();
        bankAccount2.setTransactions(Collections.singletonList(transaction3));

        User user = new User();
        user.setBankAccounts(Arrays.asList(bankAccount1, bankAccount2));

        analyticsService.analyzePerformance(user);
    }
}