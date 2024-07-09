import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.plaf.PanelUI;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static gigabank.accountmanagement.service.TransactionService.transactionCategories;

public class AnalyticServiceTest {
    // Инициализируем сразу, потому что используется один экземпляр класса для всех методов
    private AnalyticsService analyticsService = new AnalyticsService();
    private User user = new User();

    //
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;

    // Денежные константы
    private static final BigDecimal FiVE_DOLLARS = new BigDecimal("5.00");
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");

    // Константы временных промежутков
    private static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1L);
    private static final LocalDateTime FOUR_DAYS_AGO = LocalDateTime.now().minusWeeks(4L);
    private static final LocalDateTime ONE_WEEK_AGO = LocalDateTime.now().minusWeeks(1L);
    private static final LocalDateTime ONE_MONTH_AGO = LocalDateTime.now().minusMonths(1L);

    // Константы категорий транзакций
    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String HEALTH_CATEGORY = "Health";
    private static final String EDUCATION_CATEGORY = "Education";


    @Before
    public void setUp() throws Exception {
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();

        bankAccount1.getTransactions().add(new Transaction("1", TEN_DOLLARS, TransactionType.PAYMENT,
                BEAUTY_CATEGORY, ONE_DAY_AGO));
        bankAccount1.getTransactions().add(new Transaction("2", FIFTEEN_DOLLARS, TransactionType.PAYMENT,
                HEALTH_CATEGORY, ONE_WEEK_AGO));

        bankAccount2.getTransactions().add(new Transaction("3", FiVE_DOLLARS, TransactionType.PAYMENT,
                BEAUTY_CATEGORY, ONE_MONTH_AGO));
        bankAccount2.getTransactions().add(new Transaction("4", TEN_DOLLARS, TransactionType.PAYMENT,
                EDUCATION_CATEGORY, FOUR_DAYS_AGO));

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
    }

    /**
     * Получаем траты за месяц, по переданной в параметры метода категории
     */
    @Test
    public void getMonthlySpendingByCategoryTest() {
        BigDecimal result =
                analyticsService.getMonthlySpendingByCategory(bankAccount1, BEAUTY_CATEGORY);
        Assert.assertEquals(TEN_DOLLARS, result);
    }

    /**
     * Банк аккаунт = null, должно вернуть BigDecimal.ZERO
     */
    @Test
    public void getMonthlySpendingByCategoryInvalidUser() {
        BigDecimal result =
                analyticsService.getMonthlySpendingByCategory(null, BEAUTY_CATEGORY);
        Assert.assertEquals(BigDecimal.ZERO, result);
    }

    /**
     * Нет транзакций с указанной категорией или типа транзакции PAYMENT
     */
    @Test
    public void getMonthlySpendingByCategoryInvalidCategory() {
        BigDecimal result =
                analyticsService.getMonthlySpendingByCategory(bankAccount2, HEALTH_CATEGORY);
        Assert.assertEquals(BigDecimal.ZERO, result);
    }

    /**
     * Выводим сумму потраченых средств по категориям со всех банковских аккаунтов
     */
    @Test
    public void getMonthlySpendingByCategories() {
        Map<String, BigDecimal> result =
                analyticsService.getMonthlySpendingByCategories(user, transactionCategories);

        Assert.assertNotNull(result);
        Assert.assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY));
        Assert.assertEquals(FIFTEEN_DOLLARS, result.get(HEALTH_CATEGORY));
        Assert.assertEquals(TEN_DOLLARS, result.get(EDUCATION_CATEGORY));
    }

    /**
     * Пользователь = null, должно вернуть пустой HashMap
     */
    @Test
    public void getMonthlySpendingByCategoriesInvalidUser() {
        Map<String, BigDecimal> monthlySpendingByCategories =
                analyticsService.getMonthlySpendingByCategories(null, transactionCategories);

        Assert.assertEquals(new HashMap<>(), monthlySpendingByCategories);
    }

    /**
     * Вывод транзакций по категориям, отсортированные от наибольше к наименьшей
     */
    @Test
    public void getTransactionHistorySortedByAmount() {
        LinkedHashMap<String, List<Transaction>> result =
                analyticsService.getTransactionHistorySortedByAmount(user);

        Assert.assertNotNull(result);

        Assert.assertEquals(1, result.get(HEALTH_CATEGORY).size());
        Assert.assertEquals(2, result.get(BEAUTY_CATEGORY).size());
        Assert.assertEquals(1, result.get(EDUCATION_CATEGORY).size());

        Assert.assertEquals(FIFTEEN_DOLLARS, result.get(HEALTH_CATEGORY).get(0).getValue());
        Assert.assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY).get(0).getValue());
        Assert.assertEquals(TEN_DOLLARS, result.get(EDUCATION_CATEGORY).get(0).getValue());
    }

    /**
     * Пользователь равен null, должны получить пустой LinkedHashMap
     */
    @Test
    public void getTransactionHistorySortedByAmountInvalidUser() {
        LinkedHashMap<String, List<Transaction>> result =
                analyticsService.getTransactionHistorySortedByAmount(null);
        Assert.assertEquals(new LinkedHashMap<>(), result);
    }

    /**
     * Вывод последних N транзакций пользователя
     */
    @Test
    public void getTransactionListByIdentification() {
        LinkedHashMap<LocalDateTime, Transaction> result
                = analyticsService.getTransactionListByIdentification(user, 4);

        Assert.assertEquals(4, result.size());
    }

    /**
     * Пользователь равен null, получаем пустой LinkedHashMap
     */
    @Test
    public void getTransactionListByIdentificationInvalidUser() {
        LinkedHashMap<LocalDateTime, Transaction> result
                = analyticsService.getTransactionListByIdentification(null, 4);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * Получаем n самых больших платёжный транзакций пользователя
     */
    @Test
    public void getLargestUserTransaction() {
        PriorityQueue<Transaction> result
                = analyticsService.getLargestUserTransaction(user, 2);

        Assert.assertEquals(2, result.size());

        Transaction first = result.poll();
        Transaction second = result.poll();

        Assert.assertEquals(FIFTEEN_DOLLARS, first.getValue());
        Assert.assertEquals(TEN_DOLLARS, second.getValue());
    }

    /**
     * Пользователь равен null, и нет транзакций нужного типа
     */
    @Test
    public void getLargestUserTransactionInvalidInput() {
        PriorityQueue<Transaction> result
                = analyticsService.getLargestUserTransaction(null, 2);

        Assert.assertTrue(result.isEmpty());

        // Неверный тип транзакции
        user.getBankAccounts().clear();
        bankAccount1.getTransactions().clear();
        bankAccount1.getTransactions().add(new Transaction("5", FIFTEEN_DOLLARS, TransactionType.DEPOSIT,
                EDUCATION_CATEGORY, FOUR_DAYS_AGO));
        user.getBankAccounts().add(bankAccount1);

        result = analyticsService.getLargestUserTransaction(user, 2);
        Assert.assertTrue(result.isEmpty());

    }

    /**
     * Создаём новый банковский аккаунт, получаем true
     */
    @Test
    public void createNewBankAccount(){
        Boolean newBankAccountForUser
                = analyticsService.createNewBankAccountForUser(user, bankAccount1);
        Assert.assertTrue(newBankAccountForUser);
    }
    @Test
    public void createNewBankAccountInvalidInput(){
        // User = null, получаеми false
        Boolean newBankAccountForUser
                = analyticsService.createNewBankAccountForUser(null, bankAccount1);
        Assert.assertFalse(newBankAccountForUser);

        // Bank Account = null, получаем Fasle
        newBankAccountForUser
                = analyticsService.createNewBankAccountForUser(user, null);
        Assert.assertFalse(newBankAccountForUser);
    }

    /**
     * Пополнение банковского аккаунта
     */
    @Test
    public void replenishmentBankAccount(){
        Transaction transaction = new Transaction("3", TEN_DOLLARS, TransactionType.PAYMENT,
                BEAUTY_CATEGORY, ONE_MONTH_AGO);

        Boolean result = analyticsService.replenishmentBankAccount(bankAccount1, TEN_DOLLARS, transaction);
        Assert.assertTrue(result);
        Assert.assertEquals(TEN_DOLLARS, transaction.getValue());
    }

    /**
     *  Удаляем банковский аккаунт, должны получить true
     */
    @Test
    public void deleteBankAccount(){
        Boolean result = analyticsService.deleteUserBankAccount(user, bankAccount1);
        Assert.assertTrue(result);
    }

    /**
     * User = null, получаем false
     */
    @Test(expected = IllegalArgumentException.class)
    public void deleteBankAccountInvalidInput(){
        Boolean result = analyticsService.deleteUserBankAccount(null, bankAccount1);
        Assert.assertFalse(result);

        // Получаем ошибку IllegalArgumentException, если нет аккаунта, который мы пытаемся удалить
        result = analyticsService.deleteUserBankAccount(user, null);
        Assert.assertFalse(result);
    }


    @Test
    public void replenishmentBankAccountInvalidInput(){
        // Transaction = null, возвращает false
        Boolean result = analyticsService.replenishmentBankAccount(bankAccount1, TEN_DOLLARS, null);
        Assert.assertFalse(result);

        Transaction transaction = new Transaction("3", TEN_DOLLARS, TransactionType.PAYMENT,
                BEAUTY_CATEGORY, ONE_MONTH_AGO);

        // BankAccount = null, возвращает false
        result = analyticsService.replenishmentBankAccount(null, TEN_DOLLARS, transaction);
        Assert.assertFalse(result);
    }

    /**
     * Перевод денежных средств с банковского аккаунта
     */
    @Test
    public void paymentFromBankAccount(){
        Transaction transaction
                = new Transaction("7", TEN_DOLLARS, TransactionType.TRANSFER, EDUCATION_CATEGORY, ONE_DAY_AGO);

        bankAccount1.setBalance(FIFTEEN_DOLLARS); // Устанавливаем баланс аккаунта на 15 долларов

        Boolean result = analyticsService.paymentFromBankAccount(bankAccount1, transaction);// Перевод транзакции на 10 долларов

        Assert.assertTrue(result);
        Assert.assertEquals(FiVE_DOLLARS, bankAccount1.getBalance()); // Получаем остаток 5 долларов
    }

    /**
     * Перевод денежных средств, сумма транзакции превышает сумма баланса, возвращает false
     */
    @Test
    public void paymentFromBankAccountInvalidInput(){
        Transaction transaction
                = new Transaction("7", TEN_DOLLARS, TransactionType.TRANSFER, EDUCATION_CATEGORY, ONE_DAY_AGO);

        bankAccount1.setBalance(FiVE_DOLLARS); // Устанавливаем баланс аккаунта на 5 долларов
        Boolean result = analyticsService.paymentFromBankAccount(bankAccount1, transaction); // Перевод транзакции на 10 долларов
        Assert.assertFalse(result);
    }

    /**
     * Перевод денежных средств с аккаунта на другой аккаунт
     */
    @Test
    public void paymentFromAndToAccount(){
        /**
         *  Устанавливаем баланс для банковских аккаунтов
         *  1) = 15 долларов
         *  2) = 10 долларов
         *  Переводим 10 долларов с первого банковского аккаунта на второй, вернёт true, т.к сумма баланса больше суммы перевода
          */
        bankAccount1.setBalance(FIFTEEN_DOLLARS);
        bankAccount2.setBalance(TEN_DOLLARS);

        Boolean result = analyticsService.paymentFromAndToAccount(bankAccount1, bankAccount2, TEN_DOLLARS);
        Assert.assertTrue(result);
    }
    @Test
    public void paymentFromAndToAccountInvalidInput(){
        /**
         *  Устанавливаем баланс для банковских аккаунтов
         *  1) = 5 долларов
         *  2) = 10 долларов
         *  Переводим 5 долларов с первого банковского аккаунта на второй, вернёт false, т.к сумма баланса меньше суммы перевода
          */
        bankAccount1.setBalance(FiVE_DOLLARS);
        bankAccount2.setBalance(TEN_DOLLARS);

        Boolean result = analyticsService.paymentFromAndToAccount(bankAccount1, bankAccount2, TEN_DOLLARS);
        Assert.assertFalse(result);
    }

}
