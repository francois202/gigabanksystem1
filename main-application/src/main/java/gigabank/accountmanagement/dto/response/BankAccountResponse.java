package gigabank.accountmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private boolean blocked;
    private List<TransactionResponse> transactions;
}
