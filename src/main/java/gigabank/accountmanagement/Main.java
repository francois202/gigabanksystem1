package gigabank.accountmanagement;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.BankManager;
import gigabank.accountmanagement.service.PaymentGatewayService;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.notification.SmsNotificationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.properties")
public class Main {

    @Bean
    public PaymentGatewayService paymentGatewayService() {
        return new PaymentGatewayService();
    }

    @Bean
    public BankAccountService bankAccountService() {
        return new BankAccountService(paymentGatewayService(), notificationService());
    }

    @Bean
    public NotificationService notificationService() {
        return new SmsNotificationService(); // или emailNotificationService()
    }

    @Bean
    public BankManager bankManager() {
        return new BankManager(bankAccountService(), paymentGatewayService(), notificationService());
    }

    public static void main(String[] args) {

        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);

        BankManager bankManager = context.getBean(BankManager.class);
        BankAccountService accountService = context.getBean(BankAccountService.class);

        // Тестовые данные
        List<UserRequest> requests = List.of(
                new UserRequest(123, new BigDecimal("100.00"), "CARD",
                        Map.of("cardNumber", "4111111111111111", "merchant", "Amazon")),
                new UserRequest(123, new BigDecimal("200.00"), "BANK",
                        Map.of("bankName", "Chase", "accountNumber", "12345678")),
                new UserRequest(999, new BigDecimal("50.00"), "WALLET",  // Несуществующий аккаунт
                        Map.of("walletId", "wallet123")),
                new UserRequest(123, new BigDecimal("150.00"), "WALLET",
                        Map.of("walletId", "wallet456"))
        );

        // Обработка запросов
        bankManager.doWork(requests);
    }
}