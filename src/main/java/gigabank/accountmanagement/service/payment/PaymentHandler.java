package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.enums.PaymentType;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;

import java.math.BigDecimal;

public class PaymentHandler implements ServiceHandler {
    private final BankAccountService bankAccountService;
    private final PaymentGatewayService payService;
    private final ExternalNotificationService notificationService;

    public PaymentHandler(BankAccountService bankAccountService, PaymentGatewayService payService,
                          ExternalNotificationService notificationService) {
        this.bankAccountService = bankAccountService;
        this.payService = payService;
        this.notificationService = notificationService;
    }

    @Override
    public void processPayment(BankAccount account, BigDecimal amount, String authorizationId, PaymentType paymentType) {
        payService.authorize(authorizationId, amount);
        bankAccountService.withdraw(account, amount);

        System.out.println(paymentType.toString() + " pay " + account.getId());

        notificationService.sendSms(account.getOwner().getPhoneNumber(), "paid " + amount);
        notificationService.sendEmail(account.getOwner().getEmail(), "payment", paymentType + " pay " + amount);


    }
}
