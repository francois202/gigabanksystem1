import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationAdapter;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogExecutionTimeTest {
    private BankAccountService bankAccountService;
    private ByteArrayOutputStream outputStream;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private NotificationAdapter notificationAdapter;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        bankAccountService = new BankAccountService(paymentGatewayService, notificationAdapter);
    }

    @Test
    void testLogExecutionTimeExecutesWithoutErrors() {
        // Arrange
        User user = new User();
        user.setId("user-1");
        user.setPhoneNumber("+1234567890");
        user.setEmail("user@example.com");
        BankAccount account = new BankAccount("account-1", new ArrayList<>());
        account.setOwner(user);
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "OnlineStore");
        details.put("category", "Shopping");

        // Act
        assertDoesNotThrow(() -> bankAccountService.processPayment(account, new BigDecimal("100.00"), new CardPaymentStrategy(), details));

        // Assert
        String logs = outputStream.toString();
        assertTrue(logs.contains("Вызов метода: processPayment"));
        assertTrue(logs.contains("Завершение метода: processPayment, Duration:"));
    }

    @Test
    void testLogExecutionTimeDoesNotAlterBehavior() {
        // Arrange
        User user = new User();
        user.setId("user-1");
        user.setPhoneNumber("+1234567890");
        user.setEmail("user@example.com");
        BankAccount account = new BankAccount("account-1", new ArrayList<>());
        account.setOwner(user);
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "OnlineStore");
        details.put("category", "Shopping");

        // Act
        bankAccountService.processPayment(account, new BigDecimal("100.00"), new CardPaymentStrategy(), details);

        // Assert
        assertEquals(1, account.getTransactions().size());
        assertEquals(new BigDecimal("100.00"), account.getTransactions().get(0).getValue());
        assertEquals("Shopping", account.getTransactions().get(0).getCategory());
    }
}