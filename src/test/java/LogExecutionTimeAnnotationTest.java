import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LogExecutionTimeAnnotationTest {
    private static final BigDecimal TEN_DOLLARS = new BigDecimal("10.00");
    private static final BigDecimal TWENTY_DOLLARS = new BigDecimal("20.00");
    private static final String BEAUTY_CATEGORY = "Beauty";
    private static final String FOOD_CATEGORY = "Food";
    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);

    private AnalyticsService analyticsService;
    private User user;
    private BankAccount bankAccount;

    // Перехват вывода в System.out
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        // Настройка перехвата System.out
        System.setOut(new PrintStream(outContent));

        // Инициализация сервиса
        TransactionService transactionService = new TransactionService();
        analyticsService = new AnalyticsService(transactionService);

        // Настройка тестовых данных
        user = new User();
        bankAccount = new BankAccount("1", new ArrayList<>());

        // Добавляем транзакции
        bankAccount.getTransactions().add(new Transaction(
                "1",
                TEN_DOLLARS,
                TransactionType.PAYMENT,
                BEAUTY_CATEGORY,
                bankAccount,
                LocalDateTime.now(), // В пределах последнего месяца
                "Beauty Store",
                "7230",
                "1234-5678-9012-3456",
                "GigaBank",
                null
        ));
        bankAccount.getTransactions().add(new Transaction(
                "2",
                TWENTY_DOLLARS,
                TransactionType.PAYMENT,
                FOOD_CATEGORY,
                bankAccount,
                TEN_DAYS_AGO, // В пределах последнего месяца
                "Grocery Store",
                "5411",
                "9876-5432-1098-7654",
                "GigaBank",
                null
        ));

        user.setBankAccounts(new ArrayList<>());
        user.getBankAccounts().add(bankAccount);
    }

    @AfterEach
    public void tearDown() {
        // Восстановление System.out
        System.setOut(originalOut);
    }

    @Test
    public void testLogExecutionTimeNoErrors() {
        // Проверяем, что метод с @LogExecutionTime выполняется без исключений
        assertDoesNotThrow(() -> {
            analyticsService.getMonthlySpendingByCategory(bankAccount, BEAUTY_CATEGORY);
        });

        // Проверяем, что лог содержит информацию о времени выполнения
        String logOutput = outContent.toString();
        assertTrue(logOutput.contains("Продолжительность"),
                "Лог должен содержать информацию о времени выполнения");
    }

    @Test
    public void testLogExecutionTimeBehaviorUnchanged() {
        // Вызываем метод с @LogExecutionTime
        BigDecimal result = analyticsService.getMonthlySpendingByCategory(bankAccount, BEAUTY_CATEGORY);

        // Проверяем, что метод возвращает ожидаемый результат
        assertEquals(TEN_DOLLARS, result, "Сумма расходов по категории 'Beauty' должна быть 10.00");

        // Проверяем, что лог содержит информацию о времени выполнения
        String logOutput = outContent.toString();
        assertTrue(logOutput.contains("Продолжительность"),
                "Лог должен содержать информацию о времени выполнения");

        // Дополнительно проверяем для другой категории
        BigDecimal foodResult = analyticsService.getMonthlySpendingByCategory(bankAccount, FOOD_CATEGORY);
        assertEquals(TWENTY_DOLLARS, foodResult, "Сумма расходов по категории 'Food' должна быть 20.00");
    }
}