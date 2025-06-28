package gigabank.accountmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionBuilder {
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private BankAccount bankAccount;
    private LocalDateTime createdDate;
    private String merchantName;
    private String merchantCategoryCode;
    private String cardNumber;
    private String bankName;
    private String digitalWalletId;

    public TransactionBuilder id(String id) {
        this.id = id;
        return this;
    }

    public TransactionBuilder value(BigDecimal value) {
        this.value = value;
        return this;
    }

    public TransactionBuilder type(TransactionType type) {
        this.type = type;
        return this;
    }

    public TransactionBuilder category(String category) {
        this.category = category;
        return this;
    }

    public TransactionBuilder bankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
        return this;
    }

    public TransactionBuilder createdDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public TransactionBuilder merchantName(String merchantName) {
        this.merchantName = merchantName;
        return this;
    }

    public TransactionBuilder merchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
        return this;
    }

    public TransactionBuilder cardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public TransactionBuilder bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public TransactionBuilder digitalWalletId(String digitalWalletId) {
        this.digitalWalletId = digitalWalletId;
        return this;
    }


    public Transaction build() {
        return new Transaction(id, value, type, category, bankAccount, createdDate, merchantName, merchantCategoryCode, cardNumber, bankName, digitalWalletId);

    }
}



