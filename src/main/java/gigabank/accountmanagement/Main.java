package gigabank.accountmanagement;

import gigabank.accountmanagement.config.AppConfig;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.*;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletPaymentStrategy;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        try {
            // Получаем бины из контекста
            BankAccountService bankAccountService = context.getBean(BankAccountService.class);
            CardPaymentStrategy cardStrategy = context.getBean(CardPaymentStrategy.class);
            DigitalWalletPaymentStrategy walletStrategy = context.getBean(DigitalWalletPaymentStrategy.class);
            BankTransferStrategy transferStrategy = context.getBean(BankTransferStrategy.class);

            // Создаем тестовые данные
            User user = new User();
            user.setId("user-1");
            user.setPhoneNumber("+1234567890");
            user.setEmail("user@example.com");

            BankAccount account = new BankAccount("account-1", new ArrayList<>());
            account.setOwner(user);
            account.setBalance(new BigDecimal("200.00"));

            // Тест платежа с картой
            Map<String, String> details = new HashMap<>();
            details.put("cardNumber", "1234-5678-9012-3456");
            details.put("merchantName", "OnlineStore");
            details.put("category", "Shopping");
            bankAccountService.processPayment(account,new BigDecimal("100.00"),cardStrategy,details);

            // Тест платежа с цифровым кошельком
            Map<String, String> walletDetails = new HashMap<>();
            walletDetails.put("digitalWalletId", "wallet-123");
            bankAccountService.processPayment(account, new BigDecimal("150.00"), walletStrategy, walletDetails);

            // Тест платежа банковским переводом
            Map<String, String> transferDetails = new HashMap<>();
            transferDetails.put("bankName", "BigBank");
            bankAccountService.processPayment(account, new BigDecimal("200.00"), transferStrategy, transferDetails);

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        } finally {
            // Закрытие контекста (опционально)
            context.close();
        }
    }
}