package gigabank.accountmanagement.config;

import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.BankManager;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.notification.SmsNotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import gigabank.accountmanagement.service.payment.PaymentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public PaymentGatewayService paymentGatewayService() {
        return new PaymentGatewayService();
    }

    @Bean
    public NotificationService notificationService() {
        return new SmsNotificationService();
    }

    @Bean
    public BankAccountService bankAccountService() {
        return new BankAccountService(notificationService(), paymentGatewayService());
    }

    @Bean
    public PaymentHandler paymentHandler() {
        return new PaymentHandler(bankAccountService(), paymentGatewayService(), notificationService());
    }

    @Bean
    public BankManager bankManager() {
        return new BankManager(bankAccountService(), paymentHandler());
    }
}
