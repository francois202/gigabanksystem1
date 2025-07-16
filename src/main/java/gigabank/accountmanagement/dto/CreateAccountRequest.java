package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull(message = "Идентификатор пользователя не может быть пустым")
        @Positive(message = "Идентификатор пользователя должен быть положительным числом")
        int userId,

        @NotNull(message = "Начальный баланс не может быть пустым")
        @Min(value = 0, message = "Начальный баланс не может быть отрицательным")
        BigDecimal initialBalance
) {
}