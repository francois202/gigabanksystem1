package gigabank.accountmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Информация о совершенной банковской транзакции
 */
@Data
@AllArgsConstructor
public class Transaction{
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
//    private BankAccount bankAccount;
    private LocalDateTime createdDate;
}
