package gigabank.accountmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull
    private int fromAccountId;

    @NotNull
    private int toAccountId;

    @NotNull
    @DecimalMin("0.1")
    private BigDecimal amount;

    @Min(1)
    private String description;
}
