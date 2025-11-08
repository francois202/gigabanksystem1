package gigabank.accountmanagement.dto.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import gigabank.accountmanagement.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMessage {
    private Long id;
    private BigDecimal value;
    private TransactionType type;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdDate;

    private String sourceAccount;
    private String targetAccount;
    private Long bankAccountId;
}
