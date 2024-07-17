package gigabank.test.transactionservice;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static gigabank.test.TestUtils.*;

public class TransferTransactionTest {
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
        bankAccountTest1.setBalance(THIRTY_DOLLARS);
        bankAccountTest3.setBalance(BigDecimal.ZERO);
        transactionService.transferTransaction(bankAccountTest1, bankAccountTest3, TWENTY_DOLLARS);
        BigDecimal newBalance1 = bankAccountTest1.getBalance();
        BigDecimal newBalance2 = bankAccountTest3.getBalance();
        assertEquals(TEN_DOLLARS, newBalance1);
        assertEquals(TWENTY_DOLLARS, newBalance2);
    }

    @Test
    void failIfBankAccountBalanceZero() {
        boolean successTransfer = true;
        bankAccountTest1.setBalance(BigDecimal.ZERO);
        successTransfer = transactionService.transferTransaction(bankAccountTest1, bankAccountTest3, TWENTY_DOLLARS);
        assertFalse(successTransfer);
    }

    @Test
    void failIfTransferifAmountZero() {
        boolean successTransfer = true;
        successTransfer = transactionService.transferTransaction(bankAccountTest1, bankAccountTest3, BigDecimal.ZERO);
        assertFalse(successTransfer);
    }

    @Test
    void checkCreatedTransferTransaction() {
        bankAccountTest1.getTransactions().clear();
        bankAccountTest1.setBalance(THIRTY_DOLLARS);
        bankAccountTest3.getTransactions().clear();
        bankAccountTest3.setBalance(BigDecimal.ZERO);

        transactionService.transferTransaction(bankAccountTest1, bankAccountTest3, TWENTY_DOLLARS);

        Transaction lastTransactionFrom = bankAccountTest1.getTransactions()
                .get(bankAccountTest1.getTransactions().size() - 1);
        Transaction lastTransactionTo = bankAccountTest3.getTransactions()
                .get(bankAccountTest3.getTransactions().size() - 1);

        assertEquals(TWENTY_DOLLARS, lastTransactionFrom.getValue());
        assertEquals(TransactionType.TRANSFER, lastTransactionFrom.getType());
        assertEquals("Transfer", lastTransactionFrom.getCategory());

        assertEquals(TWENTY_DOLLARS, lastTransactionTo.getValue());
        assertEquals(TransactionType.TRANSFER, lastTransactionTo.getType());
        assertEquals("Transfer", lastTransactionTo.getCategory());
    }

    @Test
    void checkInputNull() {
        boolean successTransfer1 = true;
        boolean successTransfer2 = true;
        boolean successTransfer3 = true;
        BigDecimal amountNull = null;

        successTransfer1 = transactionService.transferTransaction(bankAccountNull, bankAccountTest3, TWENTY_DOLLARS);
        successTransfer2 = transactionService.transferTransaction(bankAccountTest1, bankAccountNull, TWENTY_DOLLARS);
        successTransfer3 = transactionService.transferTransaction(bankAccountTest1, bankAccountTest3, amountNull);

        assertFalse(successTransfer1);
        assertFalse(successTransfer2);
        assertFalse(successTransfer3);
    }
}