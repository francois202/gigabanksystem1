package gigabank.accountmanagement.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "Source account number is required")
    private String fromAccount;

    @NotBlank(message = "Target account number is required")
    private String toAccount;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}