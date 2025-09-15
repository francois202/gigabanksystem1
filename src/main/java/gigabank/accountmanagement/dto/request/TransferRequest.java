package gigabank.accountmanagement.dto.request;

import gigabank.accountmanagement.model.BankAccountEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    @NotNull
    BankAccountEntity account;

    @NotNull
    private BigDecimal amount;

    private Map<String, String> details;
}
