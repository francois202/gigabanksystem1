package gigabank.accountmanagement.dto.kafka;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class ProcessingMetrics {
    private AtomicLong totalTransactions = new AtomicLong(0);
    private AtomicLong successfulTransactions = new AtomicLong(0);
    private AtomicLong failedTransactions = new AtomicLong(0);
    private AtomicLong duplicateTransactions = new AtomicLong(0);
    private AtomicLong totalBatches = new AtomicLong(0);
    private AtomicLong successfulBatches = new AtomicLong(0);
    private AtomicLong failedBatches = new AtomicLong(0);
    private AtomicLong retryAttempts = new AtomicLong(0);
    private AtomicLong dltMessages = new AtomicLong(0);

    private double avgTransactionTimeMs = 0.0;
    private double avgBatchTimeMs = 0.0;
    private LocalDateTime startedAt = LocalDateTime.now();

    public void incrementSuccessfulTransactions() {
        successfulTransactions.incrementAndGet();
        totalTransactions.incrementAndGet();
    }

    public void incrementFailedTransactions() {
        failedTransactions.incrementAndGet();
        totalTransactions.incrementAndGet();
    }

    public void incrementDuplicateTransactions() {
        duplicateTransactions.incrementAndGet();
    }

    public void incrementSuccessfulBatches() {
        successfulBatches.incrementAndGet();
        totalBatches.incrementAndGet();
    }

    public void incrementFailedBatches() {
        failedBatches.incrementAndGet();
        totalBatches.incrementAndGet();
    }
}
