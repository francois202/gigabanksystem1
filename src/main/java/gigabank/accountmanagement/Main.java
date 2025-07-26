package gigabank.accountmanagement;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.BankManager;
import gigabank.accountmanagement.service.PaymentGatewayService;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.notification.SmsNotificationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        PaymentGatewayService paymentGateway = PaymentGatewayService.getInstance();
        ExternalNotificationService externalNotification = new ExternalNotificationService();
        NotificationAdapter notificationAdapter = new NotificationAdapter(createTestUser());
        NotificationService notificationService = new SmsNotificationService();
        BankAccountService accountService = new BankAccountService(
                paymentGateway,
                externalNotification,
                notificationAdapter,
                notificationService
        );

        BankManager bankManager = new BankManager(accountService);

        List<UserRequest> requests = List.of(
                new UserRequest(123, new BigDecimal("100.00"), "CARD",
                        Map.of("cardNumber", "4111111111111111", "merchant", "Amazon")),
                new UserRequest(123, new BigDecimal("200.00"), "BANK",
                        Map.of("bankName", "Chase", "accountNumber", "12345678")),
                new UserRequest(999, new BigDecimal("50.00"), "WALLET",
                        Map.of("walletId", "wallet123")),
                new UserRequest(123, new BigDecimal("150.00"), "WALLET",
                        Map.of("walletId", "wallet456"))
        );

        bankManager.doWork(requests);
    }

    private static User createTestUser() {
        User testUser = new User();
        testUser.setId("user123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("+1234567890");
        return testUser;
    }
}