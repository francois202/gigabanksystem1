package gigabank.accountmanagement.repository;

import gigabank.accountmanagement.model.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findByProcessedFalseOrderByCreatedAtAsc();
}
