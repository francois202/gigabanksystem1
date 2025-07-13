package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(
        @NotNull(message = "Сумма не может быть пустой")
        @Min(value = 1, message = "Сумма должна быть больше нуля")
        BigDecimal amount
) {
}