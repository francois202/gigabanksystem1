package gigabank.accountmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Информация о банковском счете пользователя
 */
@Entity
@Table(name = "bank_account")
@Data
@NoArgsConstructor
public class BankAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity owner;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @OneToMany(mappedBy = "bankAccountEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionEntity> transactionEntities = new ArrayList<>();
}
