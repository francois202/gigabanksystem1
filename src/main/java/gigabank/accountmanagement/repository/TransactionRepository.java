package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Modifying
    @Query("DELETE FROM TransactionEntity t WHERE t.bankAccountEntity.id = :accountId")
    void deleteByAccountId(@Param("accountId") Long accountId);

    @Query(value = "SELECT * FROM app_transaction t WHERE " +
            "(CAST(?1 AS TEXT) IS NULL OR t.bank_account_id = CAST(?1 AS BIGINT)) AND " +
            "(?2 IS NULL OR t.type = CAST(?2 AS TEXT)) AND " +
            "(?3 IS NULL OR t.category = CAST(?3 AS TEXT)) AND " +
            "(?4 IS NULL OR t.date >= CAST(?4 AS TIMESTAMP))",
            nativeQuery = true)
    Page<TransactionEntity> findWithFiltersNative(
            String accountIdStr,
            String typeStr,
            String category,
            String startDateStr,
            Pageable pageable);
}
