package gigabank.accountmanagement.config;

import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.PaymentGatewayService;
import gigabank.accountmanagement.service.SecurityLoggingProxy;
import gigabank.accountmanagement.service.notification.ExternalNotificationAdapter;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletPaymentStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages ={"gigabank.accountmanagement.service",
                              "gigabank.accountmanagement.entity",
})
public class AppConfig {
    @Bean
    public PaymentGatewayService paymentGatewayService() {
        return new PaymentGatewayService();
    }
    @Bean
    public ExternalNotificationService externalNotificationService() {
        return new ExternalNotificationService();
    }
    @Bean
    public BankAccountService bankAccountService(PaymentGatewayService paymentGatewayService, @Qualifier("email") NotificationAdapter emailNotificationAdapter) {
        return new BankAccountService(paymentGatewayService, emailNotificationAdapter);
    }
    @Bean
    public SecurityLoggingProxy securityLoggingProxy(BankAccountService bankAccountService) {
        return new SecurityLoggingProxy(bankAccountService);
    }
    @Bean
    public CardPaymentStrategy cardPaymentStrategy() {
        return new CardPaymentStrategy();
    }
    @Bean
    public DigitalWalletPaymentStrategy digitalWalletPaymentStrategy() {
        return new DigitalWalletPaymentStrategy();
    }
    @Bean
    public BankTransferStrategy bankTransferStrategy() {
        return new BankTransferStrategy();
    }
}