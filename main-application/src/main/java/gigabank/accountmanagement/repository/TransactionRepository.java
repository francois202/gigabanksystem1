package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("SELECT t FROM TransactionEntity t WHERE t.bankAccountEntity.id = :accountId")
    List<TransactionEntity> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT t FROM TransactionEntity t JOIN t.bankAccountEntity b JOIN b.owner u WHERE u.id = :userId")
    List<TransactionEntity> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM TransactionEntity t WHERE t.createdDate >= :from AND t.createdDate < :to")
    List<TransactionEntity> findByDateRange(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    List<TransactionEntity> findByValue(BigDecimal value);

    List<TransactionEntity> findByType(TransactionType type);

    @Transactional
    @Modifying
    @Query("DELETE FROM TransactionEntity t WHERE t.bankAccountEntity.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);

    @Query(value = "SELECT * FROM app_transaction t WHERE " +
            "(:accountIdStr IS NULL OR t.bank_account_id = CAST(:accountIdStr AS BIGINT)) AND " +
            "(:typeStr IS NULL OR t.type = CAST(:typeStr AS TEXT)) AND " +
            "(:category IS NULL OR t.category = CAST(:category AS TEXT)) AND " +
            "(:startDateStr IS NULL OR t.date >= CAST(:startDateStr AS TIMESTAMP))",
            countQuery = "SELECT count(*) FROM app_transaction t WHERE " +
                    "(:accountIdStr IS NULL OR t.bank_account_id = CAST(:accountIdStr AS BIGINT)) AND " +
                    "(:typeStr IS NULL OR t.type = CAST(:typeStr AS TEXT)) AND " +
                    "(:category IS NULL OR t.category = CAST(:category AS TEXT)) AND " +
                    "(:startDateStr IS NULL OR t.date >= CAST(:startDateStr AS TIMESTAMP))",
            nativeQuery = true)
    Page<TransactionEntity> findWithFiltersNative(@Param("accountIdStr") String accountIdStr,
                                                 @Param("typeStr") String typeStr,
                                                 @Param("category") String category,
                                                 @Param("startDateStr") String startDateStr,
                                                 Pageable pageable);
}
