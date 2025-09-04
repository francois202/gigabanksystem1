package gigabank.accountmanagement.service.payment.strategies;

import gigabank.accountmanagement.entity.BankAccountEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentStrategy {
    void process(BankAccountEntity account, BigDecimal amount, Map<String,String> details);
}
