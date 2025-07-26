package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.PaymentOptions;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletStrategy;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;

import java.util.*;

import static gigabank.accountmanagement.entity.PaymentOptions.*;

public class BankManager {
    private final BankAccountService bankAccountService;
    private Map<PaymentOptions, PaymentStrategy> strategy;

    public BankManager(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
        PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();
        ExternalNotificationService externalNotificationService = new ExternalNotificationService();
        this.strategy = Map.of(
                CARD, new CardPaymentStrategy(paymentGatewayService, externalNotificationService),
                BANK, new BankTransferStrategy(paymentGatewayService, externalNotificationService),
                WALLET, new DigitalWalletStrategy(paymentGatewayService, externalNotificationService)
        );
    }

    public void doWork(List<UserRequest> userRequests) {
        for (UserRequest request : userRequests) {
            BankAccount bankAccount = bankAccountService.findAccountById(request.getAccountId());
            if (bankAccount == null) {
                System.out.println("no access " + request.getAccountId());
                continue;
            }

            try {
                PaymentOptions option = PaymentOptions.valueOf(request.getPaymentType()); // преобразуем строку в enum
                PaymentStrategy paymentStrategy = strategy.get(option);

                if (paymentStrategy == null) {
                    System.out.println("Unsupported payment option: " + request.getPaymentType());
                    continue;
                }

                bankAccountService.processPayment(
                        bankAccount,
                        request.getAmount(),
                        paymentStrategy,
                        request.getPaymentDetails()
                );
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid payment type: " + request.getPaymentType());
            }
        }
    }
}