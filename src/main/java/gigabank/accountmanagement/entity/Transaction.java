package gigabank.accountmanagement.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
