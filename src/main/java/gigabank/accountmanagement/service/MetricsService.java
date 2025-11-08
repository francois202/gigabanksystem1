package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.kafka.ProcessingMetrics;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Service
public class MetricsService {

    private final ProcessingMetrics singleProcessingMetrics = new ProcessingMetrics();
    private final ProcessingMetrics batchProcessingMetrics = new ProcessingMetrics();

    private final ConcurrentHashMap<String, AtomicLong> processingTimes = new ConcurrentHashMap<>();

    public void recordSingleTransactionTime(long durationMs) {
        singleProcessingMetrics.setAvgTransactionTimeMs(
                calculateMovingAverage(singleProcessingMetrics.getAvgTransactionTimeMs(),
                        singleProcessingMetrics.getSuccessfulTransactions().get(),
                        durationMs)
        );
    }

    public void recordBatchProcessingTime(long durationMs, int batchSize) {
        batchProcessingMetrics.setAvgBatchTimeMs(
                calculateMovingAverage(batchProcessingMetrics.getAvgBatchTimeMs(),
                        batchProcessingMetrics.getSuccessfulBatches().get(),
                        durationMs)
        );

        double avgTransactionTimeInBatch = (double) durationMs / batchSize;
        batchProcessingMetrics.setAvgTransactionTimeMs(
                calculateMovingAverage(batchProcessingMetrics.getAvgTransactionTimeMs(),
                        batchProcessingMetrics.getSuccessfulTransactions().get(),
                        avgTransactionTimeInBatch)
        );
    }

    private double calculateMovingAverage(double currentAverage, long count, double newValue) {
        if (count == 0) {
            return newValue;
        }
        return (currentAverage * count + newValue) / (count + 1);
    }

}