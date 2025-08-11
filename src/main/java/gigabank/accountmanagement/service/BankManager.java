package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.UserRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.enums.PaymentType;
import gigabank.accountmanagement.service.payment.PaymentHandler;

import java.util.List;
import java.util.UUID;

public class BankManager {
    private final BankAccountService bankAccountService;
    private final PaymentHandler paymentHandler;

    public BankManager(BankAccountService bankAccountService, PaymentHandler paymentHandler) {
        this.bankAccountService = bankAccountService;
        this.paymentHandler = paymentHandler;
    }

    public void processRequests(List<UserRequest> requests) {
        for (UserRequest request : requests) {
            String authorizationId = UUID.randomUUID().toString();
            PaymentType paymentType = PaymentType.valueOf(request.getPaymentType());

            BankAccount account = bankAccountService.findAccountById(request.getAccountId());
            if (account == null) {
                System.out.println("No account with ID: " + request.getAccountId());
                continue;
            }
            paymentHandler.processPayment(account, request.getAmount(), authorizationId, paymentType);
        }
    }
}