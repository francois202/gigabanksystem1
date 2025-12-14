package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private LocalDateTime createdDate;
    private Long accountId;
    private String accountOwnerName;
}
