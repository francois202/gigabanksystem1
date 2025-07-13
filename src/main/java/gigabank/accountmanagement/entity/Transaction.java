package gigabank.accountmanagement.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Информация о совершенной банковской транзакции
 */
@Getter
@Setter
public class Transaction {
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

    public Transaction(String id, BigDecimal value, TransactionType type, String category, BankAccount bankAccount, LocalDateTime createdDate, String merchantName, String merchantCategoryCode, String cardNumber, String bankName, String digitalWalletId) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.category = category;
        this.createdDate = createdDate;
        this.bankAccount = bankAccount;
        this.merchantName = merchantName;
        this.merchantCategoryCode = merchantCategoryCode;
        this.cardNumber = cardNumber;
        this.bankName = bankName;
        this.digitalWalletId = digitalWalletId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", createdDate=" + createdDate +
                ", merchantName='" + merchantName + '\'' +
                ", merchantCategoryCode='" + merchantCategoryCode + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", bankName='" + bankName + '\'' +
                ", digitalWalletId='" + digitalWalletId + '\'' +
                '}';
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }
}