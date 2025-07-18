package gigabank.accountmanagement.paymentstrategy;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.PaymentGatewayService;
import gigabank.accountmanagement.service.ExternalNotificationService;
import gigabank.accountmanagement.notification.NotificationAdapter;

import java.math.BigDecimal;
import java.util.Map;

public class DigitalWalletStrategy implements PaymentStrategy {
    private final PaymentGatewayService paymentGatewayService;
    private final ExternalNotificationService externalNotificationService;

    public DigitalWalletStrategy(PaymentGatewayService paymentGatewayService, ExternalNotificationService externalNotificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void process(BankAccount account, BigDecimal amount, Map<String, String> details) {
        NotificationAdapter adapter = new NotificationAdapter(account.getOwner());
        System.out.println("Processed card payment for account" + account.getId());
        paymentGatewayService.authorize("Платеж цифровым кошельком", amount);
        externalNotificationService.sendSms(adapter.getPhone(), "Произошел платеж с помощью цифрового кошелька");
        externalNotificationService.sendEmail(adapter.getEmail(), "Информация о платеже", "Произошел платеж цифровым кошельком");
    }
}
