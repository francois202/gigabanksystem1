import org.example.entity.BankAccount;
import org.example.entity.Transaction;
import org.example.entity.TransactionType;
import org.example.entity.User;
import org.example.service.AnalyticsService;
import org.example.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


import static org.junit.jupiter.api.Assertions.*;


public class AnalyticsServiceTest {

    public static final Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education");

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String HEALTH_CATEGORY = "Health";
    private static final String EDUCATION_CATEGORY = "Education";
    private static final String NOT_VALID_CATEGORY = "NO_CATEGORY";

    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime FIFTEEN_DAYS_AGO = LocalDateTime.now().minusDays(15);
    private static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3);
    private static final LocalDateTime FIVE_DAYS_AGO = LocalDateTime.now().minusDays(5);
    private static final LocalDateTime ONE_DAYS_AGO = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime TWO_MONTH_AGO = LocalDateTime.now().minusMonths(2);
    private static final LocalDateTime THREE_MONTH_AGO = LocalDateTime.now().minusMonths(3);
    private static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5);

    private static final BigDecimal HUNDRED_DOLLARS = new BigDecimal("100");
    private static final BigDecimal FORTY_TWO_DOLLARS  = new BigDecimal("42");
    private static final BigDecimal FIVE_DOLLARS  = new BigDecimal("5");
    private static final BigDecimal ONE_DOLLAR  = new BigDecimal("1");
    private static final BigDecimal FIFTY_DOLLARS  = new BigDecimal("50");

    private static final BigDecimal ONE_HUNDRED_FORTY_THREE_DOLLARS = new BigDecimal("143");
    private static final BigDecimal ONE_HUNDRED_NINETY_DOLLARS = new BigDecimal("190");
    private static final BigDecimal TWO_HUNDRED_DOLLARS = new BigDecimal("200");
    private static final BigDecimal SIX_DOLLARS = new BigDecimal("6");

    private TransactionService transactionService = new TransactionService();
    private AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private User user = new User();
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;
    private BankAccount bankAccount3;
    private BankAccount bankAccountEmpty = new BankAccount();

    @BeforeEach
    public void testDataInput() {
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();
        bankAccount3 = new BankAccount();

        bankAccount1.getTransactions().addAll(Arrays.asList(
                new Transaction("1", HUNDRED_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, bankAccount1, ONE_DAYS_AGO),
                new Transaction("2", FIVE_DOLLARS, TransactionType.PAYMENT, HEALTH_CATEGORY, bankAccount1, FIFTEEN_DAYS_AGO),
                new Transaction("3", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, FIVE_DAYS_AGO),
                new Transaction("4", HUNDRED_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, THREE_DAYS_AGO),
                new Transaction("5", ONE_DOLLAR, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, TEN_DAYS_AGO),
                new Transaction("6", FIFTY_DOLLARS, TransactionType.DEPOSIT, EDUCATION_CATEGORY, bankAccount1, TEN_DAYS_AGO),
                new Transaction("7", ONE_DOLLAR, TransactionType.DEPOSIT, EDUCATION_CATEGORY, bankAccount1, TWO_MONTH_AGO),
                new Transaction("8", ONE_DOLLAR, TransactionType.DEPOSIT, HEALTH_CATEGORY, bankAccount1, FIFTEEN_DAYS_AGO),
                new Transaction("9", ONE_DOLLAR, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, THREE_MONTH_AGO),
                new Transaction("10", ONE_DOLLAR, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, FIVE_MONTHS_AGO)));

        bankAccount2.getTransactions().addAll(Arrays.asList(
                new Transaction("11", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount2, FIVE_DAYS_AGO),
                new Transaction("12", ONE_DOLLAR, TransactionType.PAYMENT, HEALTH_CATEGORY, bankAccount2, THREE_DAYS_AGO)));

        bankAccount3.getTransactions().addAll(Arrays.asList(
                new Transaction("13", HUNDRED_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, bankAccount3, THREE_DAYS_AGO),
                new Transaction("14", FIVE_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount3, TEN_DAYS_AGO)));

        user.getBankAccounts().addAll(Arrays.asList(bankAccount1, bankAccount2, bankAccount3));
    }

    @Test
    public void getMonthlySpendingByCategoryNormalTest() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, EDUCATION_CATEGORY);
        assertEquals(ONE_HUNDRED_FORTY_THREE_DOLLARS, result);
    }

    @Test
    public void getMonthlySpendingByCategoryNullTest() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, EDUCATION_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void getMonthlySpendingByCategoryNotValidCategoryTest() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, NOT_VALID_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void getMonthlySpendingByCategoryEmptyListTest() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccountEmpty, EDUCATION_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void getMonthlySpendingByCategoryNoTransactionLastMonthTest() {
        bankAccount1.getTransactions().clear();

        bankAccount1.getTransactions().addAll(Arrays.asList(
                new Transaction("15", ONE_DOLLAR, TransactionType.PAYMENT, BEAUTY_CATEGORY, bankAccount1, THREE_MONTH_AGO),
                new Transaction("16", ONE_DOLLAR, TransactionType.PAYMENT, BEAUTY_CATEGORY, bankAccount1, FIVE_MONTHS_AGO)));

        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void getMonthlySpendingByCategoriesNormalTest() {
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, TRANSACTION_CATEGORIES);
        assertEquals(ONE_HUNDRED_NINETY_DOLLARS, result.get(EDUCATION_CATEGORY));
        assertEquals(TWO_HUNDRED_DOLLARS, result.get(BEAUTY_CATEGORY));
        assertEquals(SIX_DOLLARS, result.get(HEALTH_CATEGORY));
    }

    @Test
    public void getMonthlySpendingByCategoriesNullTest() {
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(null, TRANSACTION_CATEGORIES);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getMonthlySpendingByCategoriesNotValidCategoryTest() {
        Set<String> categories = Set.of(NOT_VALID_CATEGORY);
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty());

        user.getBankAccounts().clear();
        bankAccount1.getTransactions().clear();

        bankAccount1.getTransactions().addAll(Arrays.asList(
                new Transaction("17", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, NOT_VALID_CATEGORY, bankAccount1, ONE_DAYS_AGO),
                new Transaction("18", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, NOT_VALID_CATEGORY, bankAccount1, THREE_DAYS_AGO),
                new Transaction("19", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, NOT_VALID_CATEGORY, bankAccount1, FIVE_DAYS_AGO)));

        user.getBankAccounts().add(bankAccount1);

        result = analyticsService.getMonthlySpendingByCategories(user, TRANSACTION_CATEGORIES);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getMonthlySpendingByCategoriesEmptyListTest() {
        Set<String> categoriesEmpty = new HashSet<>();
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categoriesEmpty);
        assertEquals(new HashMap<>(), result);

        user.getBankAccounts().clear();

        bankAccount1.getTransactions().clear();
        bankAccount2.getTransactions().clear();
        bankAccount3.getTransactions().clear();

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
        user.getBankAccounts().add(bankAccount3);

        result = analyticsService.getMonthlySpendingByCategories(user, TRANSACTION_CATEGORIES);
        assertEquals(new HashMap<>(), result);
    }

    @Test
    public void getMonthlySpendingByCategoriesNoTransactionLastMonthTest() {
        user.getBankAccounts().clear();

        bankAccount1.getTransactions().clear();
        bankAccount2.getTransactions().clear();

        bankAccount1.getTransactions().addAll(Arrays.asList(
                new Transaction("20", HUNDRED_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, bankAccount1, TWO_MONTH_AGO),
                new Transaction("21", FIVE_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount1, THREE_MONTH_AGO)));

        bankAccount2.getTransactions().addAll(Arrays.asList(
                new Transaction("22", FORTY_TWO_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, bankAccount2, FIVE_MONTHS_AGO),
                new Transaction("23", ONE_DOLLAR, TransactionType.PAYMENT, HEALTH_CATEGORY, bankAccount2, TWO_MONTH_AGO)));

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);

        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, TRANSACTION_CATEGORIES);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTransactionHistorySortedByAmountNormalTest() {
        user.getBankAccounts().clear();

        user.getBankAccounts().add(bankAccount2);
        user.getBankAccounts().add(bankAccount3);

        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);

        assertEquals(result.get(EDUCATION_CATEGORY).get(0).getId(), "11");
        assertEquals(result.get(EDUCATION_CATEGORY).get(1).getId(), "14");
    }

    @Test
    public void getTransactionHistorySortedByAmountNullTest() {
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTransactionHistorySortedByAmountEmptyListTest() {
        user.getBankAccounts().clear();

        bankAccount1.getTransactions().clear();
        bankAccount2.getTransactions().clear();

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);

        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getLastNTransactionNormalTest() {
        user.getBankAccounts().removeFirst();
        user.getBankAccounts().get(1).getTransactions().removeFirst();

        List<Transaction> result = analyticsService.getLastNTransaction(user, 2);

        assertEquals(2, result.size());

        assertEquals("12", result.get(0).getId());
        assertEquals("11", result.get(1).getId());

    }

    @Test
    public void getLastNTransactionNullTest() {
        List<Transaction> result = analyticsService.getLastNTransaction(null, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getLastNTransactionEmptyListTest() {
        user.getBankAccounts().clear();

        bankAccount1.getTransactions().clear();
        bankAccount2.getTransactions().clear();

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);


        List<Transaction> result = analyticsService.getLastNTransaction(user, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getLastNTransactionZeroNTest() {
        List<Transaction> result = analyticsService.getLastNTransaction(user, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTopNLargestTransactionsNormalTest() {
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 2);
        assertEquals(2, result.size());

        Transaction transaction1 = result.poll();
        Transaction transaction2 = result.poll();

        assertEquals(HUNDRED_DOLLARS, transaction1.getValue());
        assertEquals(HUNDRED_DOLLARS, transaction2.getValue());

    }

    @Test
    public void getTopNLargestTransactionsNullTest() {
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTopNLargestTransactionsEmptyListTest() {
        user.getBankAccounts().clear();

        bankAccount1.getTransactions().clear();
        bankAccount2.getTransactions().clear();

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);

        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 4);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTopNLargestTransactionsZeroNTest() {
        PriorityQueue<Transaction> result = analyticsService.getTopNLargestTransactions(user, 0);
        assertTrue(result.isEmpty());
    }


}
