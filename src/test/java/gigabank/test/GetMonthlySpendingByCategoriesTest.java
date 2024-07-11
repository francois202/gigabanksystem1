package gigabank.test;

import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetMonthlySpendingByCategoriesTest {
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
    void mustGetSumLastMonthTransactionsByBeautyCategories() {
        BigDecimal healthSum = BigDecimal.ZERO;
        BigDecimal beautySum = BigDecimal.ZERO;
        BigDecimal educationSum = BigDecimal.ZERO;

        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userIvan, TransactionService.TRANSACTION_CATEGORIES);

        for (Map.Entry<String, BigDecimal> sum : monthlySpendingMap.entrySet()) {
            if (sum.getKey().equals(HEALTH_CATEGORY)) {
                healthSum = sum.getValue();
            }
            if (sum.getKey().equals(BEAUTY_CATEGORY)) {
                beautySum = sum.getValue();
            }
            if (sum.getKey().equals(EDUCATION_CATEGORY)) {
                educationSum = sum.getValue();
            }
        }
        assertEquals(TWENTY_DOLLARS, healthSum);
        assertEquals(TEN_DOLLARS, beautySum);
        assertEquals(BigDecimal.ZERO, educationSum);
    }

    @Test
    void getEmptyMapIfInputCategoriesNotValid() {
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userNull, TransactionService.TRANSACTION_CATEGORIES);

        assertTrue(monthlySpendingMap.isEmpty());
    }

    @Test
    void getEmptyMapIfInputUserNull() {
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userNull, TransactionService.TRANSACTION_CATEGORIES);

        assertTrue(monthlySpendingMap.isEmpty());
    }

    @Test
    void checkInputCategoriesNull() {
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userIvan, null);

        assertTrue(monthlySpendingMap.isEmpty());
    }
}
