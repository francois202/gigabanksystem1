package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String FOOD_CATEGORY = "Food";
    private static final String EDUCATION_CATEGORY = "Education";

    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10L);
    private static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5L);
    private static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3L);
    private static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1L);
    private static final LocalDateTime FIVE_WEEKS_AGO = LocalDateTime.now().minusWeeks(5L);

    private TransactionService transactionService = new TransactionService();
    private AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private User user = new User();
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;
    private BankAccount bankAccount3;
    private BankAccount bankAccount4;

    @BeforeEach
    public void setUp() {
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();
        bankAccount3 = new BankAccount();
        bankAccount4 = new BankAccount();

        bankAccount1.getTransactions().add(new Transaction("1", TEN_DOLLARS, TransactionType.PAYMENT
                , BEAUTY_CATEGORY, TEN_DAYS_AGO));
        bankAccount1.getTransactions().add(new Transaction("2", FIFTEEN_DOLLARS, TransactionType.PAYMENT
                , BEAUTY_CATEGORY, FIVE_MONTHS_AGO));
        bankAccount2.getTransactions().add(new Transaction("3", TWENTY_DOLLARS, TransactionType.PAYMENT
                , FOOD_CATEGORY, THREE_DAYS_AGO));
        bankAccount2.getTransactions().add(new Transaction("4", TWENTY_DOLLARS, TransactionType.PAYMENT
                , EDUCATION_CATEGORY, ONE_DAY_AGO));
        bankAccount3.getTransactions().add(new Transaction("5", TEN_DOLLARS, TransactionType.PAYMENT
                , EDUCATION_CATEGORY, FIVE_MONTHS_AGO));
        bankAccount3.getTransactions().add(new Transaction("6", TWENTY_DOLLARS, TransactionType.PAYMENT
                , EDUCATION_CATEGORY, FIVE_WEEKS_AGO));
        bankAccount4.getTransactions().add(new Transaction("7", FIFTEEN_DOLLARS, TransactionType.DEPOSIT
                , BEAUTY_CATEGORY, ONE_DAY_AGO));
        bankAccount4.getTransactions().add(new Transaction("8", TEN_DOLLARS, TransactionType.DEPOSIT
                , FOOD_CATEGORY, THREE_DAYS_AGO));


        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
    }

    @Test
    void getMonthlySpendingByCategory() {
        // Транзакции за последний месяц существуют и принадлежат указанной категории.
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(TEN_DOLLARS, result);
        // Счет равен null.
        BigDecimal result1 = analyticsService.getMonthlySpendingByCategory(null, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result1);
        // Категории в списке нет.
        BigDecimal result2 = analyticsService.getMonthlySpendingByCategory(bankAccount1, FOOD_CATEGORY);
        assertEquals(BigDecimal.ZERO, result2);
        // Нет транзакций за последний месяц, принадлежащих указанной категории.
        BigDecimal result3 = analyticsService.getMonthlySpendingByCategory(bankAccount3, EDUCATION_CATEGORY);
        assertEquals(BigDecimal.ZERO, result3);
        // Нет транзакций типа PAYMENT.
        BigDecimal result4 = analyticsService.getMonthlySpendingByCategory(bankAccount4, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result4);

    }

    @Test
    void getMonthlySpendingByCategories() {
        // Транзакции за последний месяц существуют и принадлежат указанным категориям.
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user
                , Set.of(FOOD_CATEGORY, BEAUTY_CATEGORY));

        Assertions.assertTrue(result.containsKey(BEAUTY_CATEGORY), " Map should contains 'Beauty' ");
        // ??? Assertions.assertTrue(result.containsKey(FOOD_CATEGORY), " Map should not contains 'Food' ");
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY));
        // ??? assertEquals(TWENTY_DOLLARS, result.get(FOOD_CATEGORY));

        // Нет транзакций за последний месяц, принадлежащих указанным категориям.
        User testUser = new User();
        testUser.getBankAccounts().add(bankAccount1);
        Map<String, BigDecimal> result1 = analyticsService.getMonthlySpendingByCategories(testUser
                , Set.of(FOOD_CATEGORY, EDUCATION_CATEGORY));
        assertNull(result1.get(FOOD_CATEGORY));

        // Нет транзакций типа PAYMENT.
        User noPaymentUser = new User();
        noPaymentUser.getBankAccounts().add(bankAccount4);
        Map<String, BigDecimal> result2 = analyticsService.getMonthlySpendingByCategories(noPaymentUser
                , Set.of(BEAUTY_CATEGORY, FOOD_CATEGORY));
        assertNull(result2.get(BEAUTY_CATEGORY));
    }

    @Test
    void getTransactionHistorySortedByAmount() {
        // Транзакции за последний месяц существуют и принадлежат указанным категориям.
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);
        assertNotNull(result);
        assertEquals(1, result.get(FOOD_CATEGORY).size());
        assertEquals(2, result.get(BEAUTY_CATEGORY).size());
        assertEquals(1, result.get(EDUCATION_CATEGORY).size());

        assertEquals(TWENTY_DOLLARS, result.get(FOOD_CATEGORY).get(0).getValue());
        assertEquals(TWENTY_DOLLARS, result.get(EDUCATION_CATEGORY).get(0).getValue());
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY).get(0).getValue());

        // Нет транзакций типа PAYMENT.
        User noPayment = new User();
        noPayment.getBankAccounts().add(bankAccount4);
        LinkedHashMap<String, List<Transaction>> result1 = analyticsService.getTransactionHistorySortedByAmount(noPayment);
        assertNull(result1.get(FOOD_CATEGORY));
        assertNull(result1.get(BEAUTY_CATEGORY));

        // Пользователь равен null.
        LinkedHashMap<String, List<Transaction>> result2 = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(result2.isEmpty());
    }

    @Test
    void getLastNTransaction() {
        // Последние N транзакций существуют.
        List<Transaction> result = analyticsService.getLastNTransaction(user, 2);
        assertEquals(2, result.size());
        assertEquals("4", result.get(0).getId());
        assertEquals("3", result.get(1).getId());

        // Менее N транзакций существует.
        List<Transaction> result1 = analyticsService.getLastNTransaction(user, 7);
        assertEquals(4, result1.size());
        assertEquals("4", result1.get(0).getId());
        assertEquals("3", result1.get(1).getId());
        assertEquals("1", result1.get(2).getId());
        assertEquals("2", result1.get(3).getId());

        // Нет транзакций.
        List<Transaction> result2 = analyticsService.getLastNTransaction(new User(),4);
        assertEquals(0, result2.size());

        // Пользователь равен null.
        List<Transaction> result3 = analyticsService.getLastNTransaction(null, 2);
        assertEquals(0, result3.size());

    }

    @Test
    void getTopNLargestTransactions() {
        // Самые большие N транзакций существуют.
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 2);
        assertEquals(2, result.size());
        Transaction first = result.poll();
        Transaction second = result.poll();
        assertEquals(TWENTY_DOLLARS, first.getValue());
        assertEquals(TWENTY_DOLLARS, second.getValue());

        // Менее N транзакций существует.
        PriorityQueue<Transaction> result1 = analyticsService.getTopNLargestTransactions(user, 7);
        assertEquals(4, result1.size());

        // Нет транзакций типа PAYMENT.
        User noPaymentUser = new User();
        noPaymentUser.getBankAccounts().add(bankAccount4);
        PriorityQueue<Transaction> result2  = analyticsService.getTopNLargestTransactions(noPaymentUser, 5);
        assertEquals(0, result2.size());

        // Пользователь равен null.
        PriorityQueue<Transaction> result3 = analyticsService.getTopNLargestTransactions(null, 10);
        assertEquals(0, result3.size());

    }
}