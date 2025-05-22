package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccount;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import gigabank.accountmanagement.service.notification.NotificationService;

import java.util.logging.Logger;

@Component
public class BankManager {
    private static final Logger logger = Logger.getLogger(BankManager.class.getName());

    private final BankAccountService bankAccountService;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;

    @Autowired
    public BankManager(
            BankAccountService bankAccountService,
            PaymentGatewayService paymentGatewayService,
            @Qualifier("smsNotificationService") NotificationService notificationService) {
        this.bankAccountService = bankAccountService;
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
    }

    public void doWork(List<UserRequest> requests) {
        for (UserRequest request : requests) {
            BankAccount account = bankAccountService.findAccountById(request.getAccountId());

            if (account == null) {
                logger.warning("Account not found: " + request.getAccountId());
                continue;
            }

            processPayment(request, account);
        }
    }

    private void processPayment(UserRequest request, BankAccount account) {
        paymentGatewayService.authorize("tx", request.getAmount());
        bankAccountService.withdraw(account, request.getAmount());

        String paymentMessage = String.format("%s payment of %s for account %s",
                request.getPaymentType(), request.getAmount(), account.getId());

        switch (request.getPaymentType()) {
            case "CARD":
                logger.info("Processing card payment: " + paymentMessage);
                break;
            case "BANK":
                logger.info("Processing bank transfer: " + paymentMessage);
                break;
            case "WALLET":
                logger.info("Processing wallet payment: " + paymentMessage);
                break;
            default:
                logger.warning("Unknown payment type: " + request.getPaymentType());
                return;
        }

        notificationService.sendNotification(
                account.getOwner().getPhoneNumber(),
                String.format("Processed %s payment of %s", request.getPaymentType(), request.getAmount())
        );
    }
}