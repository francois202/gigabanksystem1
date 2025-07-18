package gigabank.accountmanagement.dto;

import gigabank.accountmanagement.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAccountRequest {
    @DecimalMin("0.0")
    private BigDecimal initialBalance;

    @NotNull
    private User owner;
}
