package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletStrategy;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;

import java.util.*;

public class BankManager {
    private final BankManager bankManager;
    private final BankAccountService bankAccountService;
    private Map<PaymentOptions, PaymentStrategy> strategy = new HashMap<>();
    private final String CARD = "CARD";
    private final String BANK = "BANK";
    private final String WALLET = "WALLET";

    public BankManager(BankManager bankManager, BankAccountService bankAccountService) {
        this.bankManager = bankManager;
        this.bankAccountService = bankAccountService;
        PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();
        ExternalNotificationService externalNotificationService = new ExternalNotificationService();
        this.strategy = Map.of(
                PaymentOptions.CARD, new CardPaymentStrategy(paymentGatewayService, externalNotificationService),
                PaymentOptions.BANK, new BankTransferStrategy(paymentGatewayService, externalNotificationService),
                PaymentOptions.WALLET, new DigitalWalletStrategy(paymentGatewayService, externalNotificationService)
        );
    }

    public void payment(List<UserRequest> userRequests) {
        for (UserRequest request : userRequests) {
            BankAccount bankAccount = bankAccountService.findAccountById(request.getAccountId());
            switch (request.getPaymentType()) {
                case CARD -> {
                    System.out.println("card pay " + bankAccount.getId());
                    bankManager.paymentCard(userRequests);
                }
                case BANK -> {
                    System.out.println("bank pay " + bankAccount.getId());
                    bankManager.paymentBank(userRequests);
                }
                case WALLET -> {
                    System.out.println("wallet pay " + bankAccount.getId());
                    bankManager.paymentWallet(userRequests);
                }
            }
        }
    }

    private void paymentCard(List<UserRequest> userRequests) {
        for (UserRequest request : userRequests) {
            BankAccount bankAccount = bankAccountService.findAccountById(request.getAccountId());
            if (bankAccount == null) {
                System.out.println("no access " + request.getAccountId());
                return;
            }

            bankAccountService.processPayment(bankAccount, request.getAmount(), strategy.get(PaymentOptions.CARD), Collections.emptyMap());
        }
    }

    private void paymentBank(List<UserRequest> userRequests) {
        for (UserRequest request : userRequests) {
            BankAccount bankAccount = bankAccountService.findAccountById(request.getAccountId());
            if (bankAccount == null) {
                System.out.println("no access " + request.getAccountId());
                return;
            }

            bankAccountService.processPayment(bankAccount, request.getAmount(), strategy.get(PaymentOptions.BANK), Collections.emptyMap());
        }
    }

    private void paymentWallet(List<UserRequest> userRequests) {
        for (UserRequest request : userRequests) {
            BankAccount bankAccount = bankAccountService.findAccountById(request.getAccountId());
            if (bankAccount == null) {
                System.out.println("no access " + request.getAccountId());
                return;
            }

            bankAccountService.processPayment(bankAccount, request.getAmount(), strategy.get(PaymentOptions.WALLET), Collections.emptyMap());
        }
    }
}