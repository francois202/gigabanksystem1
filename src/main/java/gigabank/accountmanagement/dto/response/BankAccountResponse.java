package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.model.BankAccountEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    private boolean isBlocked;
    private List<TransactionResponse> transactions;

    // Оставляем старый конструктор для обратной совместимости
    public BankAccountResponse(BankAccountEntity entity) {
        this.id = entity.getId();
        this.accountNumber = entity.getAccountNumber();
        this.balance = entity.getBalance();
        this.ownerId = entity.getOwner().getId();
        this.ownerName = entity.getOwner().getName();
        this.ownerEmail = entity.getOwner().getEmail();
        this.isBlocked = entity.isBlocked();
        this.transactions = entity.getTransactionEntities().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }
}
