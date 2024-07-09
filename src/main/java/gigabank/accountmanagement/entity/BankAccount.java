package gigabank.accountmanagement.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Информация о банковском счете пользователя
 */
@Data
public class BankAccount {
    private String id;
    private BigDecimal balance = BigDecimal.ZERO;
    private User owner;
    private List<Transaction> transactions = new ArrayList<>();
}
