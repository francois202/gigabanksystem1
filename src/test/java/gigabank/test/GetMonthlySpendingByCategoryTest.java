package gigabank.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import static gigabank.test.TestUtils.*;

public class GetMonthlySpendingByCategoryTest {
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
    void mustGetSumLastMonthTransactionsByBeautyCategory() {
        BigDecimal sum = analyticsService.getMonthlySpendingByCategory(bankAccountTest1, BEAUTY_CATEGORY);
        assertEquals(TEN_DOLLARS, sum);
    }

    @Test
    void hasNoTransactionsForTheLastMonth() {
        BigDecimal sum = analyticsService.getMonthlySpendingByCategory(bankAccountTest3, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, sum);
    }

    @Test
    void getZeroIfBankAccountIsNull() {
        BigDecimal sum = analyticsService.getMonthlySpendingByCategory(bankAccountNull, CATEGORY_NULL);
        assertEquals(BigDecimal.ZERO, sum);
    }

    @Test
    void getZeroIfCategoryIsNull() {
        BigDecimal sum = analyticsService.getMonthlySpendingByCategory(bankAccountTest3, CATEGORY_NULL);
        assertEquals(BigDecimal.ZERO, sum);
    }
}