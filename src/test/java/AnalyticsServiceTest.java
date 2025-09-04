import gigabank.accountmanagement.annotations.LogExecutionTime;
import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.entity.UserEntity;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal FIFTEEN_DOLLARS = new BigDecimal("15.00");
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");

    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String FOOD_CATEGORY = "Food";
    private static final String EDUCATION_CATEGORY = "Education";

    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final LocalDateTime FIVE_MONTHS_AGO = LocalDateTime.now().minusMonths(5);
    private static final LocalDateTime THREE_DAYS_AGO = LocalDateTime.now().minusDays(3);
    private static final LocalDateTime ONE_DAY_AGO = LocalDateTime.now().minusDays(1);

    private TransactionService transactionService = new TransactionService();
    private AnalyticsService analyticsService = new AnalyticsService(transactionService);
    private UserEntity userEntity = new UserEntity();
    private BankAccountEntity bankAccountEntity1;
    private BankAccountEntity bankAccountEntity2;

    @BeforeEach
    public void setUp() {
        bankAccountEntity1 = new BankAccountEntity();
        bankAccountEntity2 = new BankAccountEntity();
//
//        bankAccount1.getTransactions().add(new Transaction("1", TEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
//        bankAccount1.getTransactions().add(new Transaction("2", FIFTEEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, FIVE_MONTHS_AGO));
//        bankAccount2.getTransactions().add(new Transaction("3", TWENTY_DOLLARS, TransactionType.PAYMENT, FOOD_CATEGORY, THREE_DAYS_AGO));
//        bankAccount2.getTransactions().add(new Transaction("4", TWENTY_DOLLARS, TransactionType.PAYMENT, EDUCATION_CATEGORY, ONE_DAY_AGO));

        userEntity.getBankAccountEntities().add(bankAccountEntity1);
        userEntity.getBankAccountEntities().add(bankAccountEntity2);
    }

    @Test
    public void get_monthly_spending_by_category() {
        try {
            Method method = AnalyticsService.class.getMethod(
                    "getMonthlySpendingByCategory",
                    BankAccountEntity.class,
                    String.class
            );

            if (method.isAnnotationPresent(LogExecutionTime.class)) {
                long startTime = System.currentTimeMillis();
                System.out.println(method.getName() + " started");

                BigDecimal result = (BigDecimal) method.invoke(
                        analyticsService,
                        bankAccountEntity1,
                        BEAUTY_CATEGORY
                );

                long endTime = System.currentTimeMillis();
                System.out.println(method.getName() + " finished, duration: " + (endTime - startTime) + " ms");

                assertEquals(TEN_DOLLARS, result);
            }
        } catch (NoSuchMethodException e) {
            fail("Метод не найден: " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Нет доступа к методу: " + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Ошибка при вызове метода: " + e.getMessage());
        }
    }

    @Test
    public void get_monthly_spending_by_category_invalid_input() {
        // Счет равен null
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(null, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);

        // Категория равна null
        result = analyticsService.getMonthlySpendingByCategory(bankAccountEntity1, null);
        assertEquals(BigDecimal.ZERO, result);

        // Нет транзакций за последний месяц
        bankAccountEntity1.getTransactionEntities().clear();
        //bankAccount1.getTransactions().add(new Transaction("5", FIFTEEN_DOLLARS, TransactionType.PAYMENT, BEAUTY_CATEGORY, FIVE_MONTHS_AGO));
        result = analyticsService.getMonthlySpendingByCategory(bankAccountEntity1, BEAUTY_CATEGORY);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void get_transaction_history_sorted_by_amount() {
        LinkedHashMap<String, List<TransactionEntity>> result = analyticsService.getTransactionHistorySortedByAmount(userEntity);
        assertNotNull(result);
        assertEquals(1, result.get(FOOD_CATEGORY).size());
        assertEquals(2, result.get(BEAUTY_CATEGORY).size());
        assertEquals(1, result.get(EDUCATION_CATEGORY).size());

        assertEquals(TWENTY_DOLLARS, result.get(FOOD_CATEGORY).get(0).getValue());
        assertEquals(TWENTY_DOLLARS, result.get(EDUCATION_CATEGORY).get(0).getValue());
        assertEquals(TEN_DOLLARS, result.get(BEAUTY_CATEGORY).get(0).getValue());
    }

    @Test
    public void get_transaction_history_sorted_by_amount_invalid_input() {
        // Пользователь равен null
        LinkedHashMap<String, List<TransactionEntity>> result = analyticsService.getTransactionHistorySortedByAmount(null);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        userEntity.getBankAccountEntities().clear();
        bankAccountEntity1.getTransactionEntities().clear();
        //bankAccount1.getTransactions().add(new Transaction("6", TEN_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        userEntity.getBankAccountEntities().add(bankAccountEntity1);
        result = analyticsService.getTransactionHistorySortedByAmount(userEntity);
        assertTrue(result.isEmpty());
    }

    @Test
    public void get_last_n_transactions() {
        List<TransactionEntity> result = analyticsService.getLastNTransactions(userEntity, 2);
        assertEquals(2, result.size());

        assertEquals("4", result.get(0).getId());
        assertEquals("3", result.get(1).getId());
    }

    @Test
    public void get_last_n_transactions_invalid_input() {
        List<TransactionEntity> result = analyticsService.getLastNTransactions(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций
        userEntity.getBankAccountEntities().clear();
        result = analyticsService.getLastNTransactions(userEntity, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void get_top_n_largest_transactions() {

        try {
            Method method = AnalyticsService.class.getMethod(
                    "getTopNLargestTransactions",
                    UserEntity.class,
                    int.class
            );

            if (method.isAnnotationPresent(LogExecutionTime.class)) {
                long startTime = System.currentTimeMillis();
                System.out.println(method.getName() + " started");

                PriorityQueue<TransactionEntity> result = analyticsService.getTopNLargestTransactions(userEntity, 2);

                long endTime = System.currentTimeMillis();
                System.out.println(method.getName() + " finished, duration: " + (endTime - startTime) + " ms");

                assertEquals(2, result.size());

                TransactionEntity first = result.poll();
                TransactionEntity second = result.poll();

                assertEquals(TWENTY_DOLLARS, first.getValue());
                assertEquals(TWENTY_DOLLARS, second.getValue());
            }
        } catch (NoSuchMethodException e) {
            fail("Метод не найден: " + e.getMessage());
        }
    }

    @Test
    public void get_top_n_largest_transactions_invalid_input() {
        // Пользователь равен null
        PriorityQueue<TransactionEntity> result = analyticsService.getTopNLargestTransactions(null, 2);
        assertTrue(result.isEmpty());

        // Нет транзакций типа PAYMENT
        userEntity.getBankAccountEntities().clear();
        bankAccountEntity1.getTransactionEntities().clear();
        //bankAccount1.getTransactions().add(new Transaction("6", TEN_DOLLARS, TransactionType.DEPOSIT, BEAUTY_CATEGORY, TEN_DAYS_AGO));
        userEntity.getBankAccountEntities().add(bankAccountEntity1);
        result = analyticsService.getTopNLargestTransactions(userEntity, 2);
        assertTrue(result.isEmpty());
    }

    @Test
    public void analyze_performance() {

        List<TransactionEntity> transactionEntities = userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .collect(Collectors.toList());

        long startTime = System.currentTimeMillis();
        transactionEntities.stream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("1000")) > 0)
                .sorted(Comparator.comparing(TransactionEntity::getValue))
                .count();
        long endTime = System.currentTimeMillis();
        System.out.println("Sequential stream time: " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        transactionEntities.parallelStream()
                .filter(transaction -> TransactionType.PAYMENT.equals(transaction.getType()))
                .filter(transaction -> transaction.getValue().compareTo(new BigDecimal("1000")) > 0)
                .sorted(Comparator.comparing(TransactionEntity::getValue))
                .count();
        endTime = System.currentTimeMillis();
        System.out.println("Parallel stream time: " + (endTime - startTime) + " ms");
    }

    @Disabled
    @Test()
    public void process_requests_bank_manager() {
        List<UserRequest> requests = new ArrayList<>();

        requests.add(new UserRequest(
                14,
                new BigDecimal("1000.00"),
                "DEPOSIT",
                Map.of("method", "CASH", "branch", "NY-123")
        ));

        requests.add(new UserRequest(
                12345,
                new BigDecimal("500.50"),
                "WITHDRAWAL",
                Map.of("method", "ATM", "atmId", "ATM-5678")
        ));

        //BankManager bankManager = new BankManager();
        //bankManager.processRequests(requests);
    }
}
