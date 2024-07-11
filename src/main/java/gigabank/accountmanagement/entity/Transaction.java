package gigabank.accountmanagement.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Информация о совершенной банковской транзакции
 */
@Data
public class Transaction {
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
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

    /*
     * Переопределение методов hashCode() и equals() вручную.
     * Так как автоматические методы lombock вызывают рекурсию
     * при размещении в Map<User, List<BankAccount>>.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction other = (Transaction) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        String var10000 = this.getId();
        return "Transaction(id=" + var10000 + ", value=" + this.getValue() + ", type=" + this.getType() + ", category=" + this.getCategory() + ", bankAccount=" + this.getBankAccount().getId() + ", createdDate=" + this.getCreatedDate() + ")";
    }
}
