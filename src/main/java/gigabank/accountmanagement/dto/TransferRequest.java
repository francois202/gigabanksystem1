package gigabank.accountmanagement.dto;

import gigabank.accountmanagement.entity.BankAccountEntity;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class TransferRequest {
    @NotNull
    BankAccountEntity account;

    @NotNull
    private BigDecimal amount;

    private Map<String, String> details;
}
