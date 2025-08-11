package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.enums.PaymentType;

import java.math.BigDecimal;

public interface ServiceHandler {
    void processPayment(BankAccount account, BigDecimal amount, String authorizationId, PaymentType paymentType);
}
