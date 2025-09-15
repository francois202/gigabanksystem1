package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {
    BankAccountEntity findByAccountNumber(String accountNumber);
}
