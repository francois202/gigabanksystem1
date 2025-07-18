package gigabank.accountmanagement.service.paymentstrategy;

import gigabank.accountmanagement.entity.BankAccount;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentStrategy {
    void process(BankAccount account, BigDecimal amount, Map<String, String> details);
}
