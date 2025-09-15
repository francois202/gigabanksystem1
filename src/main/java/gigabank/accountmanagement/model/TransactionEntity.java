package gigabank.accountmanagement.model;

import gigabank.accountmanagement.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Информация о совершенной банковской транзакции
 */
@Entity
@Table(name = "app_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column()
    private String category;

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdDate;

    @Column(name = "source")
    private String sourceAccount;

    @Column(name = "target")
    private String targetAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccountEntity bankAccountEntity;
}
