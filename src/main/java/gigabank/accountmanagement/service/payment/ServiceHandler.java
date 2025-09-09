package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.enums.PaymentType;

import java.math.BigDecimal;

public interface ServiceHandler {
    void processPayment(BankAccountEntity account, BigDecimal amount, String authorizationId, PaymentType paymentType);
}
