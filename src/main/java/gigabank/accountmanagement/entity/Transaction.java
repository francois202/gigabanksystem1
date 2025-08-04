package gigabank.accountmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Информация о совершенной банковской транзакции
 */
@Data
@AllArgsConstructor
@Builder
public class Transaction {
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private LocalDateTime createdDate;
    private String merchantName;
    private String merchantCategoryCode;
    private String cardNumber;
    private String bankName;
    private String digitalWalletId;
}
