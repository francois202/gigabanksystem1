package gigabank.accountmanagement;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.*;
import gigabank.accountmanagement.service.notification.ExternalNotificationAdapter;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main {
    public static void main(String[] args) {
        try {
            // Создаем зависимости
            PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();
            ExternalNotificationService externalNotificationService = new ExternalNotificationService();
            NotificationAdapter notificationAdapter = new ExternalNotificationAdapter(externalNotificationService);

            // Создаем сервис
            BankAccountService bankAccountService = new BankAccountService(paymentGatewayService,notificationAdapter);

            // Создаем тестовые данные
            User user = new User();
            user.setId("user-1");
            user.setPhoneNumber("+1234567890");
            user.setEmail("user@example.com");

            BankAccount account = new BankAccount("account-1", new ArrayList<>());
            account.setOwner(user);
            account.setBalance(new BigDecimal("200.00"));

            // Тест платежа
            Map<String, String> details = new HashMap<>();
            details.put("cardNumber", "1234-5678-9012-3456");
            details.put("merchantName", "OnlineStore");
            details.put("category", "Shopping");
            bankAccountService.processPayment(account,new BigDecimal("100.00"),new CardPaymentStrategy(),details);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}