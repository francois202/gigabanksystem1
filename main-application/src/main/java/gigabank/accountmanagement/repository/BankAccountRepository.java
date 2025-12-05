package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.BankAccountEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    Optional<BankAccountEntity> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    @NotNull
    Page<BankAccountEntity> findAll(@NotNull Pageable pageable);
    List<BankAccountEntity> findAllByOwnerId(Long ownerId);
}
