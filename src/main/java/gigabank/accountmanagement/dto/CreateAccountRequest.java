package gigabank.accountmanagement.dto;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotNull
    @Size(min = 3, max = 50)
    private String ownerName;

    @Email
    private String ownerEmail;

    @Pattern(regexp = "\\+[0-9]{10,15}")
    private String ownerPhone;

    @DecimalMin("0.0")
    private BigDecimal initialBalance;
}

