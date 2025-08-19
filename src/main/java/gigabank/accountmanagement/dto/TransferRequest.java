package gigabank.accountmanagement.dto;

import gigabank.accountmanagement.entity.BankAccount;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class TransferRequest {
    @NotNull
    BankAccount account;

    @NotNull
    private BigDecimal amount;

    private Map<String, String> details;
}
