package gigabank.test.analyticsservice;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetLastNTransactionsTest {
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
    void mustSortByLastTransactions() {
        List<Transaction> sortedLastTransactions = analyticsService.getLastNTransactions(userIvan, 2);
        assertEquals(THREE_DAYS_AGO, sortedLastTransactions.get(0).getCreatedDate());
        assertEquals(TEN_DAYS_AGO, sortedLastTransactions.get(1).getCreatedDate());
    }

    @Test
    void typeOfTransactionsMustBePayment() {
        List<Transaction> sortedLastTransactions = analyticsService.getLastNTransactions(userIvan, 5);
        assertEquals(TransactionType.PAYMENT, sortedLastTransactions.get(0).getType());
        assertEquals(TransactionType.PAYMENT, sortedLastTransactions.get(1).getType());
    }

    @Test
    void getEmptyListIfInputUserNull() {
        List<Transaction> sortedLastTransactions = analyticsService.getLastNTransactions(userNull, 5);
        assertTrue(sortedLastTransactions.isEmpty());
    }

    @Test
    void getEmptyListIfInputNZEROorNegative() {
        List<Transaction> sortedLastTransactions1 = analyticsService.getLastNTransactions(userNull, 0);
        List<Transaction> sortedLastTransactions2 = analyticsService.getLastNTransactions(userNull, -5);

        assertTrue(sortedLastTransactions1.isEmpty());
        assertTrue(sortedLastTransactions2.isEmpty());
    }
}