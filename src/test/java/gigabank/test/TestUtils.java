package gigabank.test;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static gigabank.accountmanagement.service.Utils.generateTransactionId;

public class TestUtils {
    public static BankAccountService bankAccountService = new BankAccountService();
    public static TransactionService transactionService = new TransactionService();
    public static AnalyticsService analyticsService = new AnalyticsService();


    public static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    public static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");
    public static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");
    public static final BigDecimal THIRTY_DOLLARS = new BigDecimal("30.00");

    public static final String BEAUTY_CATEGORY = "Beauty";
    public static final String HEALTH_CATEGORY = "Health";
    public static final String EDUCATION_CATEGORY = "Education";
    public static final String TRANSFER_CATEGORY = "Transfer";
    public static final String CATEGORY_NULL = null;


    public static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    public static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5);
    public static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3);
    public static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1);

    public static User userIvan = new User(
            "Ivan",
            "Ivanovich",
            "Ivanov",
            LocalDate.of(1963, 7, 19));

    public static User userMaria = new User(
            "Maria",
            "Petrovna",
            "Sokolova",
            LocalDate.of(1984, 11, 1));

    public static User userNull = null;

    public static BankAccount bankAccountTest1 = new BankAccount(userIvan);
    public static BankAccount bankAccountTest2 = new BankAccount(userIvan);
    public static BankAccount bankAccountTest3 = new BankAccount(userMaria);

    public static BankAccount bankAccountNull = null;

    public static Transaction transaction1 = new Transaction(
            TEN_DOLLARS,
            TransactionType.PAYMENT,
            BEAUTY_CATEGORY,
            bankAccountTest1);

    private static void createDateTransaction1() {
        transaction1.setCreatedDate(TEN_DAYS_AGO);
    }

    public static Transaction transaction2 = new Transaction(
            FIFTEEN_DOLLARS,
            TransactionType.PAYMENT,
            BEAUTY_CATEGORY,
            bankAccountTest1);

    private static void createDateTransaction2() {
        transaction2.setCreatedDate(FIVE_MONTHS_AGO);
    }

    public static Transaction transaction3 = new Transaction(
            TWENTY_DOLLARS,
            TransactionType.PAYMENT,
            HEALTH_CATEGORY,
            bankAccountTest2);

    private static void createDateTransaction3() {
        transaction3.setCreatedDate(THREE_DAYS_AGO);
    }

    public static Transaction transaction4 = new Transaction(
            TWENTY_DOLLARS,
            TransactionType.PAYMENT,
            EDUCATION_CATEGORY,
            bankAccountTest3);

    private static void createDateTransaction4() {
        transaction4.setCreatedDate(FIVE_MONTHS_AGO);
    }

    public static Transaction transaction5 = new Transaction(
            TWENTY_DOLLARS,
            TransactionType.TRANSFER,
            TRANSFER_CATEGORY,
            bankAccountTest3);

    private static void createDateTransaction5() {
        transaction5.setCreatedDate(FIVE_MONTHS_AGO);
    }

    public static void usersInitializer() {
        userIvan.getBankAccounts().clear();
        userIvan.getBankAccounts().add(bankAccountTest1);
        userIvan.getBankAccounts().add(bankAccountTest2);
        userMaria.getBankAccounts().clear();
        userMaria.getBankAccounts().add(bankAccountTest3);
    }

    public static void bankAccountsInitializer() {
        bankAccountTest1.getTransactions().clear();
        bankAccountTest1.getTransactions().add(transaction1);
        bankAccountTest1.getTransactions().add(transaction2);
        bankAccountTest2.getTransactions().clear();
        bankAccountTest2.getTransactions().add(transaction3);
        bankAccountTest3.getTransactions().clear();
        bankAccountTest3.getTransactions().add(transaction4);
        bankAccountTest3.getTransactions().add(transaction5);
    }

    public static void transactionsInitializer() {
        createDateTransaction1();
        createDateTransaction2();
        createDateTransaction3();
        createDateTransaction4();
        createDateTransaction5();
    }

    //--------------------------------------------------------------------
    /**
     * Создаёт транзакцию со случайной суммой, а также дефолтные объекты BankAccount и User
     * для каждой новой транзакции
     * */
    public static List<Transaction> generateTransactions(int n) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            Random random = new Random();
            int randomValue = random.nextInt(1, 10000 + 1);
            String value = randomValue + ".00";
            Transaction transaction = new Transaction();
            transaction.setId(generateTransactionId());
            transaction.setValue(new BigDecimal(value));

            transactions.add(transaction);
        }

        return transactions;
    }
}