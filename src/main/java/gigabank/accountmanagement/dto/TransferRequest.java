package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Идентификатор отправителя не может быть пустым")
        String fromId,

        @NotNull(message = "Идентификатор получателя не может быть пустым")
        String toId,

        @NotNull(message = "Сумма перевода не может быть пустой")
        @Min(value = 1, message = "Сумма перевода должна быть больше нуля")
        BigDecimal amount
) {}