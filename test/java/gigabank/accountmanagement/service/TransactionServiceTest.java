package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {
    private TransactionService transactionService;
    private User user;
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;
    private BankAccount bankAccount3;

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
    private static final LocalDateTime ONE_MONTH_AGO = LocalDateTime.now().minusMonths(1L);


    @BeforeEach
    public void setUp() {
        transactionService = new TransactionService();
        user = new User();
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();
        bankAccount3 = new BankAccount();

        bankAccount1.getTransactions().add(new Transaction("1", TEN_DOLLARS, TransactionType.PAYMENT, FOOD_CATEGORY, ONE_DAY_AGO));
        bankAccount1.getTransactions().add(new Transaction("2", FIFTEEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        bankAccount1.getTransactions().add(new Transaction("3", TWENTY_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, THREE_DAYS_AGO));

        bankAccount2.getTransactions().add(new Transaction("4", TEN_DOLLARS, TransactionType.PAYMENT, FOOD_CATEGORY, FIVE_WEEKS_AGO));
        bankAccount2.getTransactions().add(new Transaction("5", TWENTY_DOLLARS, TransactionType.PAYMENT, FOOD_CATEGORY, THREE_DAYS_AGO));
        bankAccount2.getTransactions().add(new Transaction("6", FIFTEEN_DOLLARS, TransactionType.PAYMENT, FOOD_CATEGORY, FIVE_MONTHS_AGO));

        bankAccount3.getTransactions().add(new Transaction("7", FIFTEEN_DOLLARS, TransactionType.DEPOSIT, FOOD_CATEGORY, ONE_DAY_AGO));
        bankAccount3.getTransactions().add(new Transaction("8", TWENTY_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        bankAccount3.getTransactions().add(new Transaction("9", FIFTEEN_DOLLARS, TransactionType.DEPOSIT, EDUCATION_CATEGORY, THREE_DAYS_AGO));

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
        user.getBankAccounts().add(bankAccount3);

    }


    @Test
    void filterTransactions() {
        Predicate<Transaction> isOneMonthAgo = transaction -> transaction.getCreatedDate().isAfter(ONE_MONTH_AGO);
        List<Transaction> result = transactionService.filterTransactions(user, isOneMonthAgo);
        assertEquals(7, result.size());

        List<Transaction> result1 = transactionService.filterTransactions(null, isOneMonthAgo);
        assertEquals(0, result1.size());

        Predicate<Transaction> isPayment = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<Transaction> result3 = transactionService.filterTransactions(user, isPayment);
        assertEquals(6, result3.size());
    }

    @Test
    void transformTransaction() {
        Function<Transaction, String> function = transaction -> transaction.getId() + " : " + transaction.getCategory()
                + " : " + transaction.getValue();
        List<String> result = transactionService.transformTransaction(user, function);
        assertEquals(9, result.size());
        assertTrue(result.contains("1 : Food : 10.00"));
        assertTrue(result.contains("7 : Food : 15.00"));

        List<String> result1 = transactionService.transformTransaction(null, function);
        assertEquals(0, result1.size());
    }

    @Test
    void processTransactions() {
        Set<String> processCategory = new HashSet<>();
        Consumer<Transaction> consumer = transaction -> processCategory.add(transaction.getCategory());
        transactionService.processTransactions(user, consumer);
        assertEquals(3, processCategory.size());
        assertTrue(processCategory.contains("Food"));

        Set<String> process1 = new HashSet<>();
        Consumer<Transaction> consumer1 = transaction -> process1.add(transaction.getCategory());
        transactionService.processTransactions(null, consumer1);
        assertEquals(0, process1.size());
    }

    @Test
    void createTrasactionList() {
        Supplier<List<Transaction>> supplier = () -> Arrays.asList(
                new Transaction("001", new BigDecimal("100.0"), TransactionType.DEPOSIT, FOOD_CATEGORY, ONE_DAY_AGO),
                new Transaction("002", new BigDecimal("200.0"), TransactionType.PAYMENT, EDUCATION_CATEGORY, FIVE_WEEKS_AGO),
                new Transaction("003", new BigDecimal("500.0"), TransactionType.PAYMENT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        List<Transaction> result = transactionService.createTrasactionList(supplier);
        assertEquals(3, result.size());
    }

    @Test
    void mergeTransactionList() {
        List<Transaction> list1 = new ArrayList<>();
        List<Transaction> list2 = new ArrayList<>();

        list1.add(new Transaction("1", new BigDecimal("1000.00"), TransactionType.DEPOSIT, FOOD_CATEGORY, ONE_MONTH_AGO));
        list1.add(new Transaction("2", new BigDecimal("5000.0"), TransactionType.PAYMENT, EDUCATION_CATEGORY, ONE_DAY_AGO));

        list2.add(new Transaction("3", new BigDecimal("500.00"), TransactionType.PAYMENT, BEAUTY_CATEGORY, FIVE_WEEKS_AGO));
        list2.add(new Transaction("4", new BigDecimal("2000.00"), TransactionType.PAYMENT, EDUCATION_CATEGORY, THREE_DAYS_AGO));

        BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> mergeTransactions = (o1, o2) -> {
            List<Transaction> mergedList = new ArrayList<>(o1);
            mergedList.addAll(o2);
            return mergedList;
        };
        List<Transaction> result = transactionService.mergeTransactionList(list1, list2, mergeTransactions);
        assertEquals(4, result.size());

        List<Transaction> result1 = transactionService.mergeTransactionList(list1, null, mergeTransactions);
        assertEquals(0, result1.size());
    }

}