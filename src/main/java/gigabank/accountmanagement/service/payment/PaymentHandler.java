package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.enums.PaymentType;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentHandler implements ServiceHandler {
    private final BankAccountService bankAccountService;
    private final PaymentGatewayService payService;
    @Qualifier("emailNotificationService")
    private final NotificationService notificationService;

    @Autowired
    public PaymentHandler(BankAccountService bankAccountService, PaymentGatewayService payService,
                          NotificationService notificationService) {
        this.bankAccountService = bankAccountService;
        this.payService = payService;
        this.notificationService = notificationService;
    }

    @Override
    public void processPayment(BankAccount account, BigDecimal amount, String authorizationId, PaymentType paymentType) {
        payService.authorize(authorizationId, amount);
        bankAccountService.withdraw(account, amount);

        System.out.println(paymentType.toString() + " pay " + account.getId());

        notificationService.sendPaymentNotification(account.getOwner());
    }
}
