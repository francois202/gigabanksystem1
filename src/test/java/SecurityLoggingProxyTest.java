import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.*;
import gigabank.accountmanagement.service.notification.ExternalNotificationAdapter;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityLoggingProxyTest {

    private BankAccountService bankAccountService;
    private SecurityLoggingProxy securityLoggingProxy;
    private BankAccount bankAccount;
    private BigDecimal paymentAmount;
    private PaymentStrategy paymentStrategy;
    private Map<String, String> details;

    @BeforeEach
    void setUp() {
        PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();

        NotificationAdapter notificationAdapter = new NotificationAdapter() {
            @Override
            public void sendPaymentNotification(User user, String message) {
                System.out.println("Уведомление отправлено: " + message);
            }

            @Override
            public void sendRefundNotification(User user, String message) {
            System.out.println("Уведомление отправлено: " + message);
        }
        };

        bankAccountService = new BankAccountService(paymentGatewayService, notificationAdapter); {

        };
        securityLoggingProxy = new SecurityLoggingProxy(bankAccountService);
        bankAccount = new BankAccount("1", new ArrayList<>());
        bankAccount.setBalance(new BigDecimal("1000.00"));
        paymentAmount = new BigDecimal("100.00");
        paymentStrategy = new CardPaymentStrategy(); // Используем конкретную стратегию
        details = new HashMap<>();
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "Test Merchant");
    }

    @Test
    @DisplayName("Проверяет успешную обработку платежа при разрешённом доступе")
    void testProcessPaymentSucceedsWhenAccessGranted() throws NoSuchFieldException, IllegalAccessException {
        // Устанавливаем Random, который всегда возвращает true
        Random fixedRandom = new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }
        };
        setRandomField(securityLoggingProxy, fixedRandom);

        securityLoggingProxy.processPayment(bankAccount, paymentAmount, paymentStrategy, details);

        assertEquals(new BigDecimal("900.00"), bankAccount.getBalance(), "Баланс должен уменьшиться на сумму платежа");
        assertEquals(1, bankAccount.getTransactions().size(), "Необходимо добавить одну транзакцию");
    }

    @Test
    @DisplayName("Проверяет отсутствие обработки платежа при запрещённом доступе")
    void testProcessPaymentDoesNotExecuteWhenAccessDenied() throws NoSuchFieldException, IllegalAccessException {
        // Устанавливаем Random, который всегда возвращает false
        Random fixedRandom = new Random() {
            @Override
            public boolean nextBoolean() {
                return false;
            }
        };
        setRandomField(securityLoggingProxy, fixedRandom);

        securityLoggingProxy.processPayment(bankAccount, paymentAmount, paymentStrategy, details);

        assertEquals(new BigDecimal("1000.00"), bankAccount.getBalance(), "Баланс должен оставаться неизменным");
        assertEquals(0, bankAccount.getTransactions().size(), "Никакие транзакции не должны добавляться");
    }
    private void setRandomField(SecurityLoggingProxy proxy, Random random) throws NoSuchFieldException, IllegalAccessException {
        Field randomField = SecurityLoggingProxy.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(proxy, random);
        randomField.setAccessible(false);
    }
}