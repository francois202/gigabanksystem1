package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.model.BankAccountEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BankAccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private UserResponse owner;
    private boolean isBlocked;
    private List<TransactionResponse> transactions;

    public BankAccountResponse(BankAccountEntity entity) {
        this.id = entity.getId();
        this.accountNumber = entity.getAccountNumber();
        this.balance = entity.getBalance();
        this.owner = new UserResponse(entity.getOwner());
        this.isBlocked = entity.isBlocked();
        this.transactions = entity.getTransactionEntities().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}
