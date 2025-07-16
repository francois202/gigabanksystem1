import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
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
    private TransactionService transactionService = new TransactionService();
    private User user;
    private BankAccount bankAccount1;
    private BankAccount bankAccount2;

    @BeforeEach
    public void setUp() {
        transactionService = new TransactionService();
        user = new User();
        bankAccount1 = new BankAccount();
        bankAccount2 = new BankAccount();

        bankAccount1.getTransactions().add(Transaction.builder().id("1").value(new BigDecimal("100.00")).type(TransactionType.PAYMENT).category("Category1").createdDate(LocalDateTime.now()).build());
        bankAccount1.getTransactions().add(Transaction.builder().id("2").value(new BigDecimal("50.00")).type(TransactionType.PAYMENT).category("Category2").createdDate(LocalDateTime.now().minusDays(10)).build());
        bankAccount2.getTransactions().add(Transaction.builder().id("3").value(new BigDecimal("200.00")).type(TransactionType.PAYMENT).category("Category1").createdDate(LocalDateTime.now().minusMonths(1)).build());
        bankAccount2.getTransactions().add(Transaction.builder().id("4").value(new BigDecimal("150.00")).type(TransactionType.PAYMENT).category("Category3").createdDate(LocalDateTime.now().minusDays(5)).build());

        user.getBankAccounts().add(bankAccount1);
        user.getBankAccounts().add(bankAccount2);
    }

    @Test
    public void testFilterTransactions() {
        Predicate<Transaction> isPayment = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<Transaction> result = transactionService.filterTransactions(user, isPayment);
        assertEquals(4, result.size());

        Predicate<Transaction> isLargePayment = transaction -> transaction.getValue().compareTo(new BigDecimal("100.00")) > 0;
        result = transactionService.filterTransactions(user, isLargePayment);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterTransactions_nullUser() {
        Predicate<Transaction> isPayment = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<Transaction> result = transactionService.filterTransactions(null, isPayment);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTransformTransactions() {
        Function<Transaction, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> result = transactionService.transformTransactions(user, transactionToString);
        assertEquals(4, result.size());
        assertTrue(result.contains("1: 100.00"));
        assertTrue(result.contains("2: 50.00"));
        assertTrue(result.contains("3: 200.00"));
        assertTrue(result.contains("4: 150.00"));
    }

    @Test
    public void testTransformTransactions_nullUser() {
        Function<Transaction, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> result = transactionService.transformTransactions(null, transactionToString);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessTransactions() {
        List<String> processedIds = new ArrayList<>();
        Consumer<Transaction> collectTransactionIds = transaction -> processedIds.add(transaction.getId());
        transactionService.processTransactions(user, collectTransactionIds);
        assertEquals(4, processedIds.size());
        assertTrue(processedIds.contains("1"));
        assertTrue(processedIds.contains("2"));
        assertTrue(processedIds.contains("3"));
        assertTrue(processedIds.contains("4"));
    }

    @Test
    public void testProcessTransactions_nullUser() {
        List<String> processedIds = new ArrayList<>();
        Consumer<Transaction> collectTransactionIds = transaction -> processedIds.add(transaction.getId());
        transactionService.processTransactions(null, collectTransactionIds);
        assertTrue(processedIds.isEmpty());
    }

    @Test
    public void testCreateTransactionList() {
        Supplier<List<Transaction>> transactionSupplier = () -> Arrays.asList(
                Transaction.builder().id("5").value(new BigDecimal("300.00")).type(TransactionType.PAYMENT).category("Category1").createdDate(LocalDateTime.now()).build(),
                Transaction.builder().id("6").value(new BigDecimal("400.00")).type(TransactionType.PAYMENT).category("Category2").createdDate(LocalDateTime.now()).build()
        );
        List<Transaction> result = transactionService.createTransactionList(transactionSupplier);
        assertEquals(2, result.size());
        assertEquals("5", result.get(0).getId());
        assertEquals("6", result.get(1).getId());
    }
}
