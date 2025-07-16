package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;

import java.math.BigDecimal;
import java.util.Map;

public interface SecurityLogging {
    void securityLogging(BankAccount account, BigDecimal amount, PaymentStrategy strategy, Map<String, String> details);
}
