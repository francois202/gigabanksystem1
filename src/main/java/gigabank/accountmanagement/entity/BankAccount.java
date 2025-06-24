package gigabank.accountmanagement.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Информация о банковском счете пользователя
 */
@Data
public class BankAccount {
    private String id;
    private BigDecimal balance;
    private User owner;
    private List<Transaction> transactions = new ArrayList<>();

    public BankAccount(String id, List<Transaction> transactions) {
        this.id = id;
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                '}';
    }
}
