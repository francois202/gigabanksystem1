package gigabank.test.transactionservice;

import gigabank.accountmanagement.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformTransactionsTest {
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
    void mustCheckThatElementsOfListIsString() {
        Function<Transaction, String> function = Transaction::toString;
        List<String> stringTransactions = transactionService.transformTransactions(userIvan, function);

        boolean check = false;
        for (String transaction : stringTransactions) {
            if (transaction.contains("Transaction")
                    && transaction.contains("value")
                    && transaction.contains("createdDate")) {
                check = true;
                break;
            }
        }
        assertTrue(check);
    }

    @Test
    void mustGetEmptyListIfInputsNull() {
        Function<Transaction, String> function = Transaction::toString;

        List<String> withUserNull = transactionService.transformTransactions(userNull, function);
        assertTrue(withUserNull.isEmpty());

        List<String> withPredicateNull = transactionService.transformTransactions(userIvan, null);
        assertTrue(withPredicateNull.isEmpty());
    }
}