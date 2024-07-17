package gigabank.accountmanagement.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static gigabank.accountmanagement.service.Utils.*;

/**
 * Информация о банковском счете пользователя
 */
@Getter
@Setter
public class BankAccount {
    private String id = "";
    private BigDecimal balance = BigDecimal.ZERO;
    private User owner = new User();
    private final List<Transaction> transactions = new ArrayList<>();

    public BankAccount(User user) {
        if (user != null) {
            this.owner = user;
        }
        this.id = generateBankAccountId();
    }

    public BankAccount() {
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                ", owner=" + "id:" + owner.getId() + " " + owner.getFirstName() + " " + owner.getLastName() +
                ", transactions=" + transactions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankAccount that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                balance,
                owner.getId(), owner.getFirstName(), owner.getMiddleName(), owner.getLastName(), owner.getBirthDate());
    }
}