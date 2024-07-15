import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class TransactionServiceTest {
    private TransactionService transactionService = new TransactionService();
    private User user = new User();

    // Статические переменные для указания времени перевода по транзакциям
    private static final LocalDateTime ONE_DAY_AGE = LocalDateTime.now().minusDays(1L);
    private static final LocalDateTime ONE_WEEK_AGE = LocalDateTime.now().minusWeeks(1L);
    private static final LocalDateTime ONE_MONTH_AGE = LocalDateTime.now().minusMonths(1L);

    // Статические переменные категорий транзакций
    private static final String HEALTH_CATEGORY = "Health";
    private static final String EDUCATION_CATEGORY = "Education";
    private static final String BEAUTY_CATEGORY = "Beauty";

    // Статическипе переменные для указания суммый транзакций
    private static final BigDecimal ZERO_DOLLARS = new BigDecimal(0);
    private static final BigDecimal FIVE_DOLLARS = new BigDecimal(5);
    private static final BigDecimal TEN_DOLLARS = new BigDecimal(10);
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal(15);
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal(20);

    // Банковские аккаунты
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;

    // Условия выполнения интерфейса predicate (фильтрация по категории и типу транзакции)
    private Predicate<Transaction> predicate = transaction -> transaction.getCategory().equals(HEALTH_CATEGORY)
                                                              && transaction.getType().equals(TransactionType.PAYMENT);

    // Условия выполнения интерфейса function (Приводит транзакция к типу String и добавляем в List)
    private Function<Transaction, String> function = transaction -> String.valueOf(transaction);

    // Условия выполнения интерфейса consumer (Обнуляем value транзакций на 0)
    private Consumer<Transaction> consumer = transaction -> transaction.setValue(ZERO_DOLLARS);

    // Условия выполнения интерфейса BiFunction (получаем два списка и получаем объеденённый)
    private BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> biFunction =
            (list1, list2) -> {
                List<Transaction> result = new ArrayList<>(list1);
                result.addAll(list2);
                return result;
            };

    @Before
    public void setUp() {
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();

        Transaction transaction1 = new Transaction("1", ZERO_DOLLARS, TransactionType.PAYMENT, HEALTH_CATEGORY, ONE_DAY_AGE);
        Transaction transaction2 = new Transaction("2", FIVE_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, ONE_MONTH_AGE);
        Transaction transaction3 = new Transaction("3", TEN_DOLLARS, TransactionType.PAYMENT, HEALTH_CATEGORY, ONE_DAY_AGE);
        Transaction transaction4 = new Transaction("4", FIFTEEN_DOLLARS, TransactionType.DEPOSIT, HEALTH_CATEGORY, ONE_WEEK_AGE);
        Transaction transaction5 = new Transaction("5", TWENTY_DOLLARS, TransactionType.TRANSFER, EDUCATION_CATEGORY, ONE_DAY_AGE);

        bankAccount1.getTransactions().add(transaction1);
        bankAccount1.getTransactions().add(transaction2);
        bankAccount1.getTransactions().add(transaction3);

        bankAccount2.getTransactions().add(transaction4);
        bankAccount2.getTransactions().add(transaction5);

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
    }


    /**
     * Фильтруем значения по категории и типу, должны пройти проверку 2 транзакции
     */
    @Test
    public void filterTransactionsTest() {
        List<Transaction> transactions = transactionService.filterTransactions(user, predicate);

        Assert.assertEquals(2, transactions.size());
    }

    /**
     * User = null, должны получать пустой, неизменяймый List
     */
    @Test
    public void filterTransactionsTestInvalidInput() {
        List<Transaction> transactions = transactionService.filterTransactions(null, predicate);

        Assert.assertEquals(Collections.emptyList(), transactions);
    }


    /**
     * Приводим транзакции к типу String
     */
    @Test
    public void transformTransactionsTest() {
        List<String> transactions = transactionService.transformTransactions(user, function);

        Assert.assertTrue(checkTypeOfTransaction(transactions));
    }

    // Проверяем, все ли элементы в списке являются строкой
    private Boolean checkTypeOfTransaction(List<String> transactions) {
        for (String transaction : transactions) {
            return transaction instanceof String;
        }
        return false;
    }

    /**
     * User = null, должны получать пустой, неизменяймый List
     */
    @Test
    public void transformTransactionsInvalidInput() {
        List<String> transactions = transactionService.transformTransactions(null, function);

        Assert.assertEquals(Collections.emptyList(), transactions);
    }

    /**
     * Передаём consumer и обнуляем значения value у наших транзакций
     */
    @Test
    public void processTransactionsTest() {
        transactionService.processTransactions(user, consumer);

        Assert.assertEquals(BigDecimal.ZERO, user.getBankAccounts().get(0).getBalance());
    }

    /**
     * Не знаю, как проверить и с чем сравнить выполнения метода, т.к если user == null, то мы завершаем метод
     */
    @Test
    public void processTransactionsInvalidInput() {
        transactionService.processTransactions(null, consumer);
    }

    /**
     * Принимает в параметры метода два списка и возвращаем один через функциональынй интерфейс BiFunction
     */
    @Test
    public void mergeTransactionListsTest() {
        List<Transaction> transactions = transactionService.mergeTransactionLists(bankAccount1.getTransactions(),
                bankAccount2.getTransactions(), biFunction);

        // Проверяем, равен ли размер нашего списка транзакций, размеру переданных списков транзакций банковских аккаунтов
        Assert.assertEquals(bankAccount1.getTransactions().size() + bankAccount2.getTransactions().size(), transactions.size());
    }
}
