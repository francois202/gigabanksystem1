package gigabank.accountmanagement.dto.request;

import gigabank.accountmanagement.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionGenerateRequest {
    private Long transactionId;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
}
