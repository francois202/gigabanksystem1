import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.TransactionRepository;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionEntityServiceTest {
    private TransactionRepository transactionRepository;
    private TransactionService transactionService = new TransactionService(transactionRepository);
    private UserEntity userEntity;
    private BankAccountEntity bankAccountEntity1;
    private BankAccountEntity bankAccountEntity2;

    @BeforeEach
    public void setUp() {
        //transactionService = new TransactionService();
        userEntity = new UserEntity();
        bankAccountEntity1 = new BankAccountEntity();
        bankAccountEntity2 = new BankAccountEntity();

//        bankAccount1.getTransactions().add(new Transaction("1", new BigDecimal("100.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now()));
//        bankAccount1.getTransactions().add(new Transaction("2", new BigDecimal("50.00"), TransactionType.PAYMENT, "Category2", LocalDateTime.now().minusDays(10)));
//        bankAccount2.getTransactions().add(new Transaction("3", new BigDecimal("200.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now().minusMonths(1)));
//        bankAccount2.getTransactions().add(new Transaction("4", new BigDecimal("150.00"), TransactionType.PAYMENT, "Category3", LocalDateTime.now().minusDays(5)));

        userEntity.getBankAccountEntities().add(bankAccountEntity1);
        userEntity.getBankAccountEntities().add(bankAccountEntity2);
    }

    @Test
    public void testFilterTransactions() {
        Predicate<TransactionEntity> isPayment = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<TransactionEntity> result = transactionService.filterTransactions(userEntity, isPayment);
        assertEquals(4, result.size());

        Predicate<TransactionEntity> isLargePayment = transaction -> transaction.getValue().compareTo(new BigDecimal("100.00")) > 0;
        result = transactionService.filterTransactions(userEntity, isLargePayment);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterTransactions_nullUser() {
        Predicate<TransactionEntity> isPayment = transaction -> TransactionType.PAYMENT.equals(transaction.getType());
        List<TransactionEntity> result = transactionService.filterTransactions(null, isPayment);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTransformTransactions() {
        Function<TransactionEntity, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> result = transactionService.transformTransactions(userEntity, transactionToString);
        assertEquals(4, result.size());
        assertTrue(result.contains("1: 100.00"));
        assertTrue(result.contains("2: 50.00"));
        assertTrue(result.contains("3: 200.00"));
        assertTrue(result.contains("4: 150.00"));
    }

    @Test
    public void testTransformTransactions_nullUser() {
        Function<TransactionEntity, String> transactionToString = transaction -> transaction.getId() + ": " + transaction.getValue();
        List<String> result = transactionService.transformTransactions(null, transactionToString);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessTransactions() {
        List<String> processedIds = new ArrayList<>();
        Consumer<TransactionEntity> collectTransactionIds = transaction -> processedIds.add(transaction.getId().toString());
        transactionService.processTransactions(userEntity, collectTransactionIds);
        assertEquals(4, processedIds.size());
        assertTrue(processedIds.contains("1"));
        assertTrue(processedIds.contains("2"));
        assertTrue(processedIds.contains("3"));
        assertTrue(processedIds.contains("4"));
    }

    @Test
    public void testProcessTransactions_nullUser() {
        List<String> processedIds = new ArrayList<>();
        Consumer<TransactionEntity> collectTransactionIds = transaction -> processedIds.add(transaction.getId().toString());
        transactionService.processTransactions(null, collectTransactionIds);
        assertTrue(processedIds.isEmpty());
    }

//    @Test
//    public void testCreateTransactionList() {
////        Supplier<List<Transaction>> transactionSupplier = () -> Arrays.asList(
////                new Transaction("5", new BigDecimal("300.00"), TransactionType.PAYMENT, "Category1", LocalDateTime.now()),
////                new Transaction("6", new BigDecimal("400.00"), TransactionType.PAYMENT, "Category2", LocalDateTime.now())
////        );
//        List<Transaction> result = transactionService.createTransactionList(transactionSupplier);
//        assertEquals(2, result.size());
//        assertEquals("5", result.get(0).getId());
//        assertEquals("6", result.get(1).getId());
//    }
}
