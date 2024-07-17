package gigabank.test.transactionservice;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static gigabank.test.TestUtils.*;

public class PaymentTransactionTest {
    @BeforeAll
    static void Initializer() {
        usersInitializer();
        bankAccountsInitializer();
        transactionsInitializer();
    }
    @AfterEach
    void resetBankAccountBalance() {
        bankAccountTest1.setBalance(BigDecimal.ZERO);
    }

    @Test
    void bankAccountBalanceMustDecrease() {
        bankAccountTest1.setBalance(TWENTY_DOLLARS);
        transactionService.paymentTransaction(bankAccountTest1, transaction1);
        BigDecimal newBalance = bankAccountTest1.getBalance();
        assertEquals(TEN_DOLLARS, newBalance);
    }

    @Test
    void failIfBankAccountBalanceZero() {
        boolean successPayment = true;
        bankAccountTest1.setBalance(BigDecimal.ZERO);
        successPayment = transactionService.paymentTransaction(bankAccountTest1, transaction1);
        assertFalse(successPayment);
    }

    @Test
    void failIfPaymentZero() {
        boolean successPayment = true;
        Transaction transaction = new Transaction();
        transaction.setValue(BigDecimal.ZERO);
        successPayment = transactionService.paymentTransaction(bankAccountTest1, transaction);
        assertFalse(successPayment);
    }

    @Test
    void checkCreatedPaymentTransaction() {
        bankAccountTest1.getTransactions().clear();
        bankAccountTest1.setBalance(TWENTY_DOLLARS);
        transactionService.paymentTransaction(bankAccountTest1, transaction1);
        Transaction lastTransaction = bankAccountTest1.getTransactions()
                .get(bankAccountTest1.getTransactions().size()-1);

        assertEquals(TEN_DOLLARS, lastTransaction.getValue());
        assertEquals(TransactionType.PAYMENT, lastTransaction.getType());
        assertEquals("Beauty", lastTransaction.getCategory());
    }

    @Test
    void checkNullInput () {
        boolean successPayment1 = true;
        boolean successPayment2 = true;
        successPayment1 = transactionService.paymentTransaction(bankAccountNull, transaction1);
        successPayment2 = transactionService.paymentTransaction(bankAccountTest1, null);
        assertFalse(successPayment1);
        assertFalse(successPayment2);
    }
}