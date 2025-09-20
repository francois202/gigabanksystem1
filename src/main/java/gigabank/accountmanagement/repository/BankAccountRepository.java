package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.BankAccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    Optional<BankAccountEntity> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    Page<BankAccountEntity> findAll(Pageable pageable);
}
