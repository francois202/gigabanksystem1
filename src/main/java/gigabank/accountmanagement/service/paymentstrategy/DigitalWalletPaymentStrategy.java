package gigabank.accountmanagement.service.paymentstrategy;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class DigitalWalletPaymentStrategy implements PaymentStrategy {

    @Override
    public void process(BankAccount bankAccount,BigDecimal value,Map<String,String> details) {
        String id = UUID.randomUUID().toString();
        String digitalWalletId = details.get("digitalWalletId");

        Transaction transaction = Transaction.builder()
                .id(id)
                .value(value)
                .type(TransactionType.PAYMENT)
                .category("Wallet Payment")
                .bankAccount(bankAccount)
                .createdDate(LocalDateTime.now())
                .digitalWalletId(digitalWalletId)
                .build();
        bankAccount.getTransactions().add(transaction);
    }
}