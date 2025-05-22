package gigabank.accountmanagement.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DepositWithdrawRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String description;
}
