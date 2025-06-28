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
            BankAccountService bankAccountService = new BankAccountService(paymentGatewayService, notificationAdapter);

            // Создаем тестовые данные
            User user = new User();
            user.setId("user-1");
            user.setPhoneNumber("+1234567890");
            user.setEmail("user@example.com");

            BankAccount account = new BankAccount("account-1", new ArrayList<>());
            account.setOwner(user);

            // Тест платежа
            Map<String, String> details = new HashMap<>();
            details.put("cardNumber", "1234-5678-9012-3456");
            details.put("merchantName", "OnlineStore");
            details.put("category", "Shopping");
            bankAccountService.processPayment(account, new BigDecimal("100.00"), new CardPaymentStrategy(), details);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
//        try {
//            // Получаем экземпляр PaymentGatewayService
//            PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();
//
//            // Создаем экземпляр ExternalNotificationService (предполагаем, что он существует)
//            ExternalNotificationService externalNotificationService = new ExternalNotificationService() {
//                // Реализация методов интерфейса, если требуется
//                // Это заглушка для примера
//            };
//
//            // Создаем сервис с необходимыми зависимостями
//            BankAccountService bankAccountService = new BankAccountService(paymentGatewayService, externalNotificationService);
//
//            // Оборачиваем его в прокси
//            SecurityLoggingProxy proxy = new SecurityLoggingProxy(bankAccountService);
//            RefundService refundService = new RefundService();
//
//            // Создаем тестовые данные
//            User user = new User();
//            user.setId("user-1");
//            BankAccount account = new BankAccount("account-1", new ArrayList<Transaction>());
//            account.setOwner(user);
//
//            // Тест платежей через прокси
//            Map<String, String> details = new HashMap<>();
//            details.put("cardNumber", "1234-5678-9012-3456");
//            details.put("merchantName", "OnlineStore");
//            proxy.processPayment(account, new BigDecimal("100.00"), new CardPaymentStrategy(), details);
//
//            details.clear();
//            details.put("digitalWalletId", "Z8483737292992");
//            details.put("merchantName", "DigitalPay");
//            proxy.processPayment(account, new BigDecimal("249.99"), new DigitalWalletPaymentStrategy(), details);
//
//            details.clear();
//            details.put("bankName", "Tinkoff");
//            details.put("merchantName", "BankCorp");
//            proxy.processPayment(account, new BigDecimal("25000.00"), new BankTransferStrategy(), details);
//
//            // Тест возврата (без прокси, так как не указано)
//            details.clear();
//            details.put("cardNumber", "1234-5678-9012-3456");
//            details.put("merchantName", "OnlineStore");
//            refundService.processRefund(account, new BigDecimal("50.00"), details);
//
//            // Вывод транзакций
//            System.out.println("Transactions: " + account.getTransactions());
//        } catch (Exception e) {
//            System.out.println("Ошибка: " + e.getMessage());
//        }
//    }
//}





