package gigabank.test;

import gigabank.accountmanagement.entity.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetTransactionHistorySortedByAmountTest {
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
    void mustSortTransactionByTopAmount() {
        LinkedHashMap<String, List<Transaction>> transactionsMap = analyticsService
                .getTransactionHistorySortedByAmount(userIvan);

        String topCategory = transactionsMap.keySet().iterator().next();
        BigDecimal topAmount = BigDecimal.ZERO;
        for (Map.Entry <String, List<Transaction>> transaction : transactionsMap.entrySet()) {
            if(transaction.getKey().contains(topCategory)) {
                topAmount = transaction.getValue().get(0).getValue();
            }
        }
        assertEquals(HEALTH_CATEGORY, topCategory);
        assertEquals(TWENTY_DOLLARS, topAmount);
    }

    @Test
    void getEmptyMapIfInputUserNull() {
        LinkedHashMap<String, List<Transaction>> transactionsMap = analyticsService
                .getTransactionHistorySortedByAmount(userNull);

        assertTrue(transactionsMap.isEmpty());
    }
}
