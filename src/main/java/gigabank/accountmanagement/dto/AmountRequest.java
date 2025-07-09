package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AmountRequest {
    @NotNull(message = "Сумма не может быть пустой")
    @Min(value = 1, message = "Сумма должна быть больше нуля")
    private BigDecimal amount;
}