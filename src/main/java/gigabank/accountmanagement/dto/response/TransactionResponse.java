package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.TransactionEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private LocalDateTime createdDate;

    public TransactionResponse(TransactionEntity entity) {
        this.id = entity.getId();
        this.value = entity.getValue();
        this.type = entity.getType();
        this.category = entity.getCategory();
        this.createdDate = entity.getCreatedDate();
    }
}
