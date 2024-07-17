package gigabank.test.transactionservice;

import gigabank.accountmanagement.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterTransactionsTest {
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
    void mustGetFilteredTransactionsByBeautyCategory() {
        Predicate<Transaction> predicate = t -> t.getCategory().equals("Beauty");

        List<Transaction> filteredTransactions = transactionService.filterTransactions(userIvan, predicate);

        int validatedTransactions = 0;
        for (Transaction transaction: filteredTransactions) {
            if(transaction.getCategory().equals("Beauty")) {
                validatedTransactions++;
            }
        }
        assertEquals(2, validatedTransactions);
    }

    @Test
    void mustGetEmptyListIfInputsNull() {
        Predicate<Transaction> predicate = t -> t.getCategory().equals("Beauty");

        List<Transaction> withUserNull = transactionService.filterTransactions(userNull, predicate);
        assertTrue(withUserNull.isEmpty());

        List<Transaction> withPredicateNull = transactionService.filterTransactions(userIvan, null);
        assertTrue(withPredicateNull.isEmpty());
    }
}