package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.kafka.ProcessingMetrics;
import gigabank.accountmanagement.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping
    public Map<String, Object> getMetrics() {
        ProcessingMetrics single = metricsService.getSingleProcessingMetrics();
        ProcessingMetrics batch = metricsService.getBatchProcessingMetrics();

        Map<String, Object> metrics = new HashMap<>();

        metrics.put("singleProcessing", Map.of(
                "totalTransactions", single.getTotalTransactions().get(),
                "successfulTransactions", single.getSuccessfulTransactions().get(),
                "failedTransactions", single.getFailedTransactions().get(),
                "duplicateTransactions", single.getDuplicateTransactions().get(),
                "retryAttempts", single.getRetryAttempts().get(),
                "dltMessages", single.getDltMessages().get(),
                "avgTransactionTimeMs", Math.round(single.getAvgTransactionTimeMs() * 100.0) / 100.0,
                "startedAt", single.getStartedAt()
        ));

        metrics.put("totalBatches", batch.getTotalBatches().get());
        metrics.put("successfulBatches", batch.getSuccessfulBatches().get());
        metrics.put("failedBatches", batch.getFailedBatches().get());
        metrics.put("totalTransactions", batch.getTotalTransactions().get());
        metrics.put("successfulTransactions", batch.getSuccessfulTransactions().get());
        metrics.put("failedTransactions", batch.getFailedTransactions().get());
        metrics.put("duplicateTransactions", batch.getDuplicateTransactions().get());
        metrics.put("avgBatchTimeMs", Math.round(batch.getAvgBatchTimeMs() * 100.0) / 100.0);
        metrics.put("avgTransactionTimeMs", Math.round(batch.getAvgTransactionTimeMs() * 100.0) / 100.0);
        metrics.put("throughput", calculateThroughput(batch));
        metrics.put("startedAt", batch.getStartedAt());

        // Сравнительные метрики
        metrics.put("comparison", Map.of(
                "singleThroughput", calculateThroughput(single),
                "batchThroughput", calculateThroughput(batch),
                "efficiencyGain", calculateEfficiencyGain(single, batch)
        ));

        return metrics;
    }

    private double calculateThroughput(ProcessingMetrics metrics) {
        long successfulTransactions = metrics.getSuccessfulTransactions().get();
        long uptimeMinutes = java.time.Duration.between(metrics.getStartedAt(), java.time.LocalDateTime.now()).toMinutes();
        return uptimeMinutes > 0 ? (double) successfulTransactions / uptimeMinutes : 0.0;
    }

    private String calculateEfficiencyGain(ProcessingMetrics single, ProcessingMetrics batch) {
        if (single.getAvgTransactionTimeMs() == 0) return "N/A";
        double gain = ((single.getAvgTransactionTimeMs() - batch.getAvgTransactionTimeMs()) / single.getAvgTransactionTimeMs()) * 100;
        return String.format("%.2f%%", gain);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }
}