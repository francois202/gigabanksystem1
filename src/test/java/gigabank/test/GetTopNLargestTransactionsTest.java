package gigabank.test;

import gigabank.accountmanagement.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.PriorityQueue;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetTopNLargestTransactionsTest {
    @BeforeAll
    static void Initializer() {
        usersInitializer();
        bankAccountsInitializer();
    }

    @AfterEach
    void resetBankAccountBalance() {
        bankAccountTest1.setBalance(BigDecimal.ZERO);
    }

    @Test
    void mustSortByLargestTransactions() {
        PriorityQueue<Transaction> topTransactions = analyticsService.getTopNLargestTransactions(userIvan, 10);

        assertEquals(TWENTY_DOLLARS, topTransactions.poll().getValue());
        assertEquals(FIFTEEN_DOLLARS, topTransactions.poll().getValue());
        assertEquals(TEN_DOLLARS, topTransactions.peek().getValue());
    }

    @Test
    void getEmptyListIfInputUserNull() {
        PriorityQueue<Transaction> topTransactions = analyticsService.getTopNLargestTransactions(userNull, 4);
        assertTrue(topTransactions.isEmpty());
    }

    @Test
    void getEmptyListIfInputNZEROorNegative() {
        PriorityQueue<Transaction> topTransactions1 = analyticsService.getTopNLargestTransactions(userIvan, 0);
        PriorityQueue<Transaction> topTransactions2 = analyticsService.getTopNLargestTransactions(userIvan, -5);

        assertTrue(topTransactions1.isEmpty());
        assertTrue(topTransactions2.isEmpty());
    }
}