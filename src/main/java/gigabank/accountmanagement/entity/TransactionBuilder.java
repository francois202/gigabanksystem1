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

    public synchronized TransactionBuilder id(String id) {
        this.id = id;
        return this;
    }

    public synchronized TransactionBuilder value(BigDecimal value) {
        this.value = value;
        return this;
    }

    public synchronized TransactionBuilder type(TransactionType type) {
        this.type = type;
        return this;
    }

    public synchronized TransactionBuilder category(String category) {
        this.category = category;
        return this;
    }

    public synchronized TransactionBuilder bankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
        return this;
    }

    public synchronized TransactionBuilder createdDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public synchronized TransactionBuilder merchantName(String merchantName) {
        this.merchantName = merchantName;
        return this;
    }

    public synchronized TransactionBuilder merchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
        return this;
    }

    public synchronized TransactionBuilder cardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public synchronized TransactionBuilder bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public synchronized TransactionBuilder digitalWalletId(String digitalWalletId) {
        this.digitalWalletId = digitalWalletId;
        return this;
    }


    public synchronized Transaction build() {
        return new Transaction(id,value,type,category,bankAccount,createdDate,merchantName,merchantCategoryCode,cardNumber,bankName,digitalWalletId);

    }
}



