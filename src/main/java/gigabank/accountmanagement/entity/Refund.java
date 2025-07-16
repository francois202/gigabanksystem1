package gigabank.accountmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Возврат по транзакции
 */
@Data
@AllArgsConstructor
public class Refund {
    private int id;
    private BigDecimal amount;
    private String description;
    private int transactionId;
}