package gigabank.accountmanagement.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

/**
 * Информация о совершенной банковской транзакции
 */
@Getter @Setter
public class Transaction {
    private String id = "";
    private BigDecimal value = BigDecimal.ZERO;
    private TransactionType type;
    private String category = "";
    private BankAccount bankAccount;
    private LocalDateTime createdDate;

    public Transaction() {
        this.id = generateTransactionId();
        this.createdDate = LocalDateTime.now();
    }

    public Transaction(BigDecimal value, TransactionType type, String category, BankAccount bankAccount) {
        this.id = generateTransactionId();
        this.value = value;
        this.type = type;
        this.category = category;
        this.bankAccount = bankAccount;
        this.createdDate = LocalDateTime.now();
    }

    public Transaction(String id, BigDecimal value, TransactionType type, String category,
                       BankAccount bankAccount, LocalDateTime createdDate) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.category = category;
        this.bankAccount = bankAccount;
        this.createdDate = createdDate;
    }

    private static String generateTransactionId() {
        Random random = new Random();
        int randomId = random.nextInt(1, 100000);
        return String.valueOf(randomId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", bankAccount id=" + bankAccount.getId() +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(value, that.value)
                && type == that.type
                && Objects.equals(category, that.category)
                && Objects.equals(bankAccount, that.bankAccount)
                && Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                value,
                type,
                category,
                bankAccount.getId(), bankAccount.getBalance(),
                createdDate);
    }
}