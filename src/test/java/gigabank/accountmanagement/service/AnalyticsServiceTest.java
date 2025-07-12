package gigabank.accountmanagement.service;

import static org.junit.jupiter.api.Assertions.*;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    private AnalyticsService analyticsService;
    private BankAccount bankAccount;
    private TransactionService transactionService;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        analyticsService = new AnalyticsService();
        bankAccount = new BankAccount("bank123");
        transactionService = new TransactionService();
    }


    // getMonthlySpendingByCategory

    @Test
    void getMonthlySpendingByCategory_NullBankAccount_ReturnsZero() { //Проверяем, что если передан не существующий банковский счет
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, "Food");
        assertEquals(BigDecimal.ZERO, result, "При null BankAccount должен возвращаться 0.");
    }

    @Test
    void getMonthlySpendingByCategory_CategoryNotInTransaction_ReturnsZero() {// Проверяем, что если нет вызываемой категории
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount, "Нет такой категории");
        assertEquals(BigDecimal.ZERO, result,"Если категории нет, то должен возвращать 0.");
    }

    @Test
    void getMonthlySpendingByCategory_NoTransactionInLastMonth_ReturnZero() { //Если ни одной транзакции не было в последнем месяце
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("bank123",BigDecimal.TEN, TransactionType.PAYMENT,"Food",LocalDateTime.now().minusMonths(2)));
        bankAccount.setTransactions(transactions);
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount, "Food");
        assertEquals(BigDecimal.ZERO, result, "Если транзакция сделана более месяца назад, должен возвращать 0.");
    }

    @Test
    void getMonthlySpendingByCategory_PymentTransactionInLastMonth_ReturnsCorrectAmount() { // Проверить есть ли транзакция платёж за последний месяц в указанной категории
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("bank123", BigDecimal.TEN, TransactionType.PAYMENT, "Food",LocalDateTime.now().minusDays(10))); // Добавляем транзакцию платёж в категории еда нынешней датой и значение равно ten
        bankAccount.setTransactions(transactions);
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount,"Food");
        assertEquals(BigDecimal.TEN, result, "Должна вернуться сумма транзакции");
    }

    @Test
    void getMonthlySpendingByCategory_ManyPymentTransactionInLastMonth_ReturnsCorrectAmount() { // Проверить считает ли сумму транзакций за последний месяц в указанной категории
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(125),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(16))); // Первая транзакция
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(235),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(20))); // Вторая транзакция с другой датой
        bankAccount.setTransactions(transactions);
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount,"Food");
        assertEquals(BigDecimal.valueOf(360),result, "Сумма транзакций должна быть");
    }

    @Test
    void getMonthlySpendingByCategory_AnotherTransactionTypeInLastMonth_OnlyPaymentTransactionsAreConsidered() {// Проверить что метод не учитывает другие типы транзакций кроме платёжных
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(125),TransactionType.INCOME, "Food", LocalDateTime.now().minusDays(16))); // Первая транзакция платёжная
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(235),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(20))); // Вторая транзакция с другой датой и другой тип платежа
        bankAccount.setTransactions(transactions);
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount,"Food");
        assertEquals(BigDecimal.valueOf(235),result, "Сумма транзакций должна быть равна только платёжной");
    }

    @Test
    void getMonthlySpendingByCategory_AnotherCategoryInLastMonth_OnlyOneCategoryISConsidered() { // Проверить что учитываются транзакции только в выбранной категории за последний месяц
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(125),TransactionType.PAYMENT, "Health", LocalDateTime.now().minusDays(16))); // Первая транзакция платёжная, но с другой категорией
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(235),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(20)));// Вторая транзакция с другой датой
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(1005),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(45))); // Третья транзакция, которая не попадает по времени
        transactions.add(new Transaction("bank123", BigDecimal.valueOf(2100),TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(5))); // Четвертая транзакция которая подходит
        bankAccount.setTransactions(transactions);
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount,"Food");
        assertEquals(BigDecimal.valueOf(2335),result, "Сумма транзакций должна быть равна только выбранной категории");
    }

    // getMonthlySpendingByCategories

    @Test
    void getMonthlySpendingByCategories_NullUser_ReturnsEmptyMap() { // Проверка если user null
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(null, categories);
        assertTrue(result.isEmpty(),"Пустая карта если нет данных пользователя");
    }

    @Test
    void getMonthlySpendingByCategories_ValidCategoriesIsEmpty_ReturnsEmptyMap() {// Проверяем если категория не передана
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of();
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта если нет категорий");
    }

    @Test
    void getMonthlySpendingByCategories_NoTransactions_ReturnsEmptyMap(){ // Проверяет есть ли вообще транзакции
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта, если нет транзакций");
    }

    @Test
    void getMonthlySpendingByCategories_NoBankAccounts_ReturnsEmptyMap(){ // Проверяет есть ли банковский аккаунт у пользователя
        user.setBankAccounts(List.of());
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта, если нет банковского счёта");
    }

    @Test
    void getMonthlySpendingByCategories_NonPaymentTransactionType_IsExcluded() { // Проверяет если тип платежа не payment
        Transaction transaction = new Transaction("bank123", BigDecimal.valueOf(1000), TransactionType.DEPOSIT, "Food", LocalDateTime.now().minusDays(5));
        bankAccount.setTransactions(List.of(transaction));
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта если транзакция не платёжная");
    }

    @Test
    void getMonthlySpendingByCategories_InvalidCategory_ReturnsEmptyMap(){ // Проверяем если категория не подходит
        Transaction transaction = new Transaction("bank123", BigDecimal.valueOf(2000), TransactionType.PAYMENT, "ValidCategory", LocalDateTime.now().minusDays(10));
        bankAccount.setTransactions(List.of(transaction));
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта если транзакция не той категории");
    }

    @Test
    void getMonthlySpendingByCategories_NotInLastMonth_ReturnsEmptyMap(){ // Проверяем если транзакция старше месяца
        Transaction transaction = new Transaction("bank123",  BigDecimal.valueOf(2000), TransactionType.PAYMENT,"Food", LocalDateTime.now().minusMonths(1));
        bankAccount.setTransactions(List.of(transaction));
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertTrue(result.isEmpty(), "Пустая карта если операция старше одного месяца");
    }

    @Test
    void getMonthlySpendingByCategories_OneValidTransaction_ReturnsCorrectMap(){ // Проверяет, что метод возвращает правильную карту с суммой для одной валидной транзакции.
        Transaction transaction = new Transaction("bank123",  BigDecimal.valueOf(2000), TransactionType.PAYMENT,"Food", LocalDateTime.now().minusDays(5));
        bankAccount.setTransactions(List.of(transaction));
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertEquals(1,result.size());
        assertEquals(BigDecimal.valueOf(2000),result.get("Food"));
    }

    @Test
    void getMonthlySpendingByCategories_MultiValidTransaction_ReturnsCorrectMap(){ // Проверяет, что метод правильно суммирует суммы для нескольких транзакций в одной и той же категории.
        List<Transaction> transaction = new ArrayList<>();
        transaction.add(new Transaction("bank123",  BigDecimal.valueOf(2000), TransactionType.PAYMENT,"Food", LocalDateTime.now().minusDays(5)));
        transaction.add(new Transaction("bank123",  BigDecimal.valueOf(3000), TransactionType.PAYMENT,"Food", LocalDateTime.now().minusDays(10)));
        bankAccount.setTransactions(transaction);
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertEquals(1,result.size()); // Проверяем, что в результате один элемент
        assertEquals(BigDecimal.valueOf(5000),result.get("Food"));
    }

    @Test
    void getMonthlySpendingByCategories_MultipleTransactionsDifferentCategories() { // Проверяем что метод суммирует выбранные категории
        List<Transaction> transaction = new ArrayList<>();
        transaction.add(new Transaction("bank123",  BigDecimal.valueOf(2000), TransactionType.PAYMENT,"Food", LocalDateTime.now().minusDays(5)));
        transaction.add(new Transaction("bank123",  BigDecimal.valueOf(3000), TransactionType.PAYMENT,"Health", LocalDateTime.now().minusDays(10)));
        transaction.add(new Transaction("bank123",  BigDecimal.valueOf(2000), TransactionType.PAYMENT,"Sport", LocalDateTime.now().minusDays(5)));
        bankAccount.setTransactions(transaction);
        user.setBankAccounts(List.of(bankAccount));
        Set<String> categories = Set.of("Food", "Health");
        Map<String, BigDecimal> result = analyticsService.getMonthlySpendingByCategories(user, categories);
        assertEquals(2, result.size()); // Проверяем размер результирующей Map
        assertEquals(BigDecimal.valueOf(2000), result.get("Food")); // Проверяем значение для Food
        assertEquals(BigDecimal.valueOf(3000), result.get("Health")); // Проверяем значение для Health
    }


    //  getTransactionHistorySortedByAmount


    @Test
    void getTransactionHistorySortedByAmount() {
    }

    @Test
    void getLastNTransaction() {
    }

    @Test
    void getTopLargestTransactions() {
    }
}

