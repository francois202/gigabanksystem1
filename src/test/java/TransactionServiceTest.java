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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionServiceTest {
    private TransactionService transactionService;
    private User testUser;
    private BankAccount firstAccount;
    private BankAccount secondAccount;

    @BeforeEach
    public void setUp() {
        transactionService = new TransactionService();
        testUser = new User();
        firstAccount = new BankAccount();
        secondAccount = new BankAccount();

        firstAccount.getTransactions().add(new Transaction("1", new BigDecimal("100.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now()));
        firstAccount.getTransactions().add(new Transaction("2", new BigDecimal("50.00"), TransactionType.PAYMENT, "Category2", LocalDateTime.now().minusDays(10)));
        secondAccount.getTransactions().add(new Transaction("3", new BigDecimal("200.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now().minusMonths(1)));
        secondAccount.getTransactions().add(new Transaction("4", new BigDecimal("150.00"), TransactionType.PAYMENT, "Category3", LocalDateTime.now().minusDays(5)));

        testUser.getBankAccounts().add(firstAccount);
        testUser.getBankAccounts().add(secondAccount);
    }

    @Test
    public void testTransactionFiltering() {
        Predicate<Transaction> isPaymentType = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<Transaction> filteredTransactions = transactionService.filterTransactions(testUser, isPaymentType);
        assertEquals(4, filteredTransactions.size());

        Predicate<Transaction> isLargePayment = transaction -> transaction.getValue().compareTo(new BigDecimal("100.00")) > 0;
        filteredTransactions = transactionService.filterTransactions(testUser, isLargePayment);
        assertEquals(2, filteredTransactions.size());
    }

    @Test
    public void testFilteringWithNullUser() {
        Predicate<Transaction> isPaymentType = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<Transaction> filteredTransactions = transactionService.filterTransactions(null, isPaymentType);
        assertTrue(filteredTransactions.isEmpty());
    }

    @Test
    public void testTransactionTransformation() {
        Function<Transaction, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> transformedTransactions = transactionService.transformTransactions(testUser, transactionToString);
        assertEquals(4, transformedTransactions.size());
        assertTrue(transformedTransactions.contains("1: 100.00"));
        assertTrue(transformedTransactions.contains("2: 50.00"));
        assertTrue(transformedTransactions.contains("3: 200.00"));
        assertTrue(transformedTransactions.contains("4: 150.00"));
    }

    @Test
    public void testTransformationWithNullUser() {
        Function<Transaction, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> transformedTransactions = transactionService.transformTransactions(null, transactionToString);
        assertTrue(transformedTransactions.isEmpty());
    }

    @Test
    public void testTransactionProcessing() {
        List<String> processedTransactionIds = new ArrayList<>();
        Consumer<Transaction> collectIds = transaction -> processedTransactionIds.add(transaction.getId());
        transactionService.processTransactions(testUser, collectIds);
        assertEquals(4, processedTransactionIds.size());
        assertTrue(processedTransactionIds.contains("1"));
        assertTrue(processedTransactionIds.contains("2"));
        assertTrue(processedTransactionIds.contains("3"));
        assertTrue(processedTransactionIds.contains("4"));
    }

    @Test
    public void testProcessingWithNullUser() {
        List<String> processedTransactionIds = new ArrayList<>();
        Consumer<Transaction> collectIds = transaction -> processedTransactionIds.add(transaction.getId());
        transactionService.processTransactions(null, collectIds);
        assertTrue(processedTransactionIds.isEmpty());
    }

    @Test
    public void testTransactionListCreation() {
        Supplier<List<Transaction>> transactionSupplier = () -> Arrays.asList(
                new Transaction("5", new BigDecimal("300.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now()),
                new Transaction("6", new BigDecimal("400.00"), TransactionType.PAYMENT, "Category2", LocalDateTime.now())
        );
        List<Transaction> createdTransactions = transactionService.createTransactionList(transactionSupplier);
        assertEquals(2, createdTransactions.size());
        assertEquals("5", createdTransactions.get(0).getId());
        assertEquals("6", createdTransactions.get(1).getId());
    }
}
