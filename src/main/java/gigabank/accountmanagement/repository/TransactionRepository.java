package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByAccountId(String accountId);
}