package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotNull(message = "Идентификатор пользователя не может быть пустым")
    private String userId;

    @NotNull(message = "Начальный баланс не может быть пустым")
    @Min(value = 0, message = "Начальный баланс не может быть отрицательным")
    private BigDecimal initialBalance;
}