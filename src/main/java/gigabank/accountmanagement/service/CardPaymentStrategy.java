package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class CardPaymentStrategy implements PaymentStrategy {
    private static final String CARD_NUMBER_KEY = "cardNumber";
    private static final String MERCHANT_NAME_KEY = "merchantName";

    @Override
    public void process(BankAccount bankAccount, BigDecimal value, Map<String, String> details) {
        String id = UUID.randomUUID().toString();
        String cardNumber = details.get(CARD_NUMBER_KEY);
        String merchantName = details.get(MERCHANT_NAME_KEY);

        Transaction transaction = Transaction.builder()
                .id(id)
                .value(value)
                .type(TransactionType.PAYMENT)
                .category("Card Payment")
                .bankAccount(bankAccount)
                .createdDate(LocalDateTime.now())
                .merchantName(merchantName)
                .cardNumber(cardNumber)
                .build();
        bankAccount.getTransactions().add(transaction);
    }
}

