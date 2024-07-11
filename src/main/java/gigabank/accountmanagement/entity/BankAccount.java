package gigabank.accountmanagement.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Информация о банковском счете пользователя
 */
@Data
public class BankAccount {
    private String id;
    private BigDecimal balance;
    private User owner;
    private List<Transaction> transactions = new ArrayList<>();

    public BankAccount(User user) {
        this.id = generateBankAccountId();
        this.balance = BigDecimal.ZERO;
        this.owner = user;
    }

    public BankAccount(String id, BigDecimal balance, User owner) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
    }

    private static String generateBankAccountId() {
        Random random = new Random();
        int randomId = random.nextInt(1, 10000);
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
        BankAccount other = (BankAccount) obj;
        return id.equals(other.id);
    }
    @Override
    public String toString() {
        String var10000 = this.getId();
        return "BankAccount(id=" + var10000 + ", balance=" + this.getBalance() + ", owner=" + this.getOwner().getFirstName() + " " + this.getOwner().getLastName() + ", transactions=" + this.getTransactions() + ")";
    }
}
