package gigabank.test.transactionservice;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessTransactionsTest {
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
    void mustModifyTransactionId() {
        Consumer<Transaction> consumer = t -> t.setId("test" + t.getId());
        transactionService.processTransactions(userIvan, consumer);

        boolean idIsModified = false;
        for (BankAccount bankAccount : userIvan.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (transaction.getId().contains("test")) {
                    idIsModified = true;
                    break;
                }
            }
        }
        assertTrue(idIsModified);
    }

    @Test
    void mustGetVoidIfInputsNull() {
        transactionService.processTransactions(userIvan, null);

        boolean idIsModified = true;
        for (BankAccount bankAccount : userIvan.getBankAccounts()) {
            for (Transaction transaction : bankAccount.getTransactions()) {
                if (!transaction.getId().contains("testNull")) {
                    idIsModified = false;
                    break;
                }
            }
        }
        assertFalse(idIsModified);
    }
}