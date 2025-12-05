package gigabank.accountmanagement.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gigabank.accountmanagement.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

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
@ToString(exclude = "bankAccountEntity")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdDate;

    @Column(name = "source")
    private String sourceAccount;

    @Column(name = "target")
    private String targetAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    @JsonIgnore
    private BankAccountEntity bankAccountEntity;
}
