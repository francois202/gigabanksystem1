package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий (Spring Data JPA)
 * */
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
}