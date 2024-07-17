package gigabank.test.analyticsservice;

import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetMonthlySpendingByCategoriesTest {
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
    void mustGetSumLastMonthTransactionsByBeautyCategories() {
        BigDecimal healthSum = BigDecimal.ZERO;
        BigDecimal beautySum = BigDecimal.ZERO;
        BigDecimal educationSum = BigDecimal.ZERO;

        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userIvan, TransactionService.TRANSACTION_CATEGORIES);

        for (Map.Entry<String, BigDecimal> categorySum : monthlySpendingMap.entrySet()) {
            if (categorySum.getKey().equals(HEALTH_CATEGORY)) {
                healthSum = categorySum.getValue();
            }
            if (categorySum.getKey().equals(BEAUTY_CATEGORY)) {
                beautySum = categorySum.getValue();
            }
            if (categorySum.getKey().equals(EDUCATION_CATEGORY)) {
                educationSum = categorySum.getValue();
            }
        }
        assertEquals(TWENTY_DOLLARS, healthSum);
        assertEquals(TEN_DOLLARS, beautySum);
        assertEquals(BigDecimal.ZERO, educationSum);
    }

    @Test
    void mustGetEmptyMapIfInputCategoriesInvalid() {
        Set<String> invalidCategories = Set.of("Sex", "Drugs", "Rock-n-Roll");
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userIvan, invalidCategories);

        assertTrue(monthlySpendingMap.isEmpty());
    }

    @Test
    void mustGetEmptyMapIfInputUserNull() {
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userNull, TransactionService.TRANSACTION_CATEGORIES);

        assertTrue(monthlySpendingMap.isEmpty());
    }

    @Test
    void mustGetEmptyMapIfInputCategoriesNull() {
        Map<String, BigDecimal> monthlySpendingMap = analyticsService.getMonthlySpendingByCategories(
                userIvan, null);

        assertTrue(monthlySpendingMap.isEmpty());
    }
}