package gigabank.accountmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Transaction {
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private BankAccount bankAccount;
    private LocalDateTime createdDate;


    public Transaction(String generatedId, BigDecimal value, TransactionType type, String category, LocalDateTime createdData) {
        this.id = generatedId;
        this.value = value;
        this.type = type;
        this.category = category;
        this.createdDate = createdData;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setValue(BigDecimal bigDecimal) {
        this.value = bigDecimal;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }


}

