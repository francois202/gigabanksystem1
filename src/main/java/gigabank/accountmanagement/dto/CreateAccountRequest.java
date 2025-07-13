package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull(message = "Идентификатор пользователя не может быть пустым")
        String userId,

        @NotNull(message = "Начальный баланс не может быть пустым")
        @Min(value = 0, message = "Начальный баланс не может быть отрицательным")
        BigDecimal initialBalance
) {
}