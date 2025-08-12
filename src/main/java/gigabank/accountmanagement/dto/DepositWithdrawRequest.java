package gigabank.accountmanagement.dto;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Data
public class DepositWithdrawRequest {
    @NotNull
    private BigDecimal amount;
    private String description;
}
