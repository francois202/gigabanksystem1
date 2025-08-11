import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class TestAnalyticsService {
    private static AnalyticsService analyticsService = new AnalyticsService();
    private static User user = new User();

    private BankAccount bankAccount1;
    private BankAccount bankAccount2;
    private BankAccount bankAccount3;

    private static final BigDecimal ONE_DOLLAR = new BigDecimal(1.0);
    private static final BigDecimal TWO_DOLLARS = new BigDecimal(2.0);
    private static final BigDecimal FIVE_DOLLARS = new BigDecimal(5.0);
    private static final BigDecimal TEN_DOLLARS = new BigDecimal(10.0);

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String HEALTH_CATEGORY = "Health";
    private static final String EDUCATION_CATEGORY = "Education";

    private static final LocalDateTime ONE_MONTH_AGO = LocalDateTime.now().minusMonths(1L);
    private static final LocalDateTime TWO_MONTH_AGO = LocalDateTime.now().minusMonths(2L);
    private static final LocalDateTime ONE_WEEK_AGO = LocalDateTime.now().minusWeeks(1L);
    private static final LocalDateTime FIVE_WEEK_AGO = LocalDateTime.now().minusWeeks(5L);

    @Before
    public void createNewBankAccount() {

        List<Transaction> testTransactionOnlyDepositType = new ArrayList<>();
        testTransactionOnlyDepositType.add(new Transaction("1", TWO_DOLLARS
                , TransactionType.DEPOSIT, BEAUTY_CATEGORY, ONE_WEEK_AGO));
        testTransactionOnlyDepositType.add(new Transaction("2", TEN_DOLLARS
                , TransactionType.DEPOSIT, HEALTH_CATEGORY, ONE_WEEK_AGO));
        testTransactionOnlyDepositType.add(new Transaction("3", ONE_DOLLAR
                , TransactionType.DEPOSIT, EDUCATION_CATEGORY, ONE_WEEK_AGO));

        List<Transaction> testTransactionDifTime = new ArrayList<>();
        testTransactionDifTime.add(new Transaction("1", TWO_DOLLARS
                , TransactionType.PAYMENT, BEAUTY_CATEGORY, ONE_MONTH_AGO));
        testTransactionDifTime.add(new Transaction("2", TEN_DOLLARS
                , TransactionType.PAYMENT, HEALTH_CATEGORY, FIVE_WEEK_AGO));
        testTransactionDifTime.add(new Transaction("3", ONE_DOLLAR
                , TransactionType.PAYMENT, EDUCATION_CATEGORY, TWO_MONTH_AGO));

        List<Transaction> TypeAndTime = Stream.concat(testTransactionOnlyDepositType.stream()
                , testTransactionDifTime.stream()).toList();

        bankAccount1 = new BankAccount("001", TypeAndTime );
        bankAccount2 = new BankAccount("002", testTransactionOnlyDepositType);
        bankAccount3 = new BankAccount("003", testTransactionDifTime);

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
        user.getBankAccounts().add(bankAccount3);

    }

    // 1 test
    /**
     * Транзакции за последний месяц существуют и принадлежат указанной категории.
     */
    @Test
    public void getMonthlySpendingByCategory() {
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        Assert.assertEquals(TWO_DOLLARS, result);
    }

    /**
     * Счет равен null,  категории в списке нет.
     */
    @Test
    public void NullUser(){
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, HEALTH_CATEGORY);
        Assert.assertEquals(BigDecimal.ZERO, result);
    }
    /**
     * Нет транзакций за последний месяц, принадлежащих указанной категории. Нет транзакций типа PAYMENT.
     */
    @Test
    public void NoPAYMENT(){
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount2, EDUCATION_CATEGORY);
        Assert.assertEquals(BigDecimal.ZERO, result);
    }

    //2
    /**
     * Транзакции за последний месяц существуют и принадлежат указанным категориям.
     */
    @Test
    public void getMonthlySpendingByCategories(){
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user
                , Set.of(HEALTH_CATEGORY, BEAUTY_CATEGORY));

        Assert.assertTrue(" Map should contains 'Beauty' ", result.containsKey(BEAUTY_CATEGORY));
        Assert.assertFalse(" Map should not contains 'Health' ", result.containsKey(HEALTH_CATEGORY));
        Assert.assertEquals(TWO_DOLLARS, result.get(BEAUTY_CATEGORY));
    }
    /**
     * Нет транзакций за последний месяц, принадлежащих указанным категориям.
     */
    @Test
    public void NoTransaction(){
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user
                , Set.of(HEALTH_CATEGORY, EDUCATION_CATEGORY));
        Assert.assertTrue(result.isEmpty());
    }
    /**
     * Нет транзакций типа PAYMENT.
     */
    @Test
    public void NoPayment(){
        User testUser = new User();
        testUser.getBankAccounts().add(bankAccount2);
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(testUser
                , Set.of (HEALTH_CATEGORY, BEAUTY_CATEGORY, EDUCATION_CATEGORY));
        Assert.assertTrue(result.isEmpty());
    }
    /**
     * Пользователь равен null.
     */
    @Test
    public void UserShouldBeNull(){
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(null
                , Set.of(HEALTH_CATEGORY, BEAUTY_CATEGORY));
        Assert.assertTrue(result.isEmpty());
    }
    /**
     * Категории не существуют.
     */
    @Test
    public void NoCategories(){
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user
                , Set.of("something"));
        Assert.assertTrue(result.isEmpty());
    }

    //3
    /**
     * Транзакции существуют и принадлежат различным категориям.
     */
    @Test
    public void getTransactionHistorySortedByAmount(){
        LinkedHashMap<String, List<Transaction>> result = analyticsService.getTransactionHistorySortedByAmount(user);
        

    }









}
