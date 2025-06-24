package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class BankTransferStrategy implements PaymentStrategy {

    @Override
    public void process(BankAccount bankAccount, BigDecimal value, Map<String, String> details) {
        String id = UUID.randomUUID().toString();
        String bankName = details.get("bankName");

        Transaction transaction = Transaction.builder()
                .id(id)
                .value(value)
                .type(TransactionType.PAYMENT)
                .category("Bank Transfer")
                .bankAccount(bankAccount)
                .createdDate(LocalDateTime.now())
                .bankName(bankName)
                .build();
        bankAccount.getTransactions().add(transaction);
    }
}
