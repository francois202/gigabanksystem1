package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.service.TransactionLoadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
public class LoadTestController {

    private final TransactionLoadService transactionLoadService;

    /**
     * Генерация нагрузки для тестирования обработки транзакций
     */
    @PostMapping("/generate-batch-load")
    public String generateBatchLoad(@RequestParam(defaultValue = "100") int totalTransactions,
                                    @RequestParam(defaultValue = "batch") String deliveryMode) {
        return transactionLoadService.generateBatchLoad(totalTransactions, deliveryMode);
    }
}