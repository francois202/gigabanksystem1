package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "Идентификатор отправителя не может быть пустым")
        String fromId,

        @NotBlank(message = "Идентификатор получателя не может быть пустым")
        String toId,

        @NotNull(message = "Сумма перевода не может быть пустой")
        @Min(value = 1, message = "Сумма перевода должна быть больше нуля")
        BigDecimal amount
) {
}