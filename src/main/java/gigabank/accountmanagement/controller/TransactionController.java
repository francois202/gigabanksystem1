package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Контроллер для управления операциями с транзакциями.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * Получает отфильтрованный список транзакций с поддержкой пагинации и сортировки.
     *
     * @param accountId идентификатор счета для фильтрации (опционально)
     * @param type тип транзакции для фильтрации (опционально)
     * @param category категория транзакции для фильтрации (опционально)
     * @param startDate начальная дата для фильтрации по периоду (опционально)
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 20)
     * @param sortBy поле для сортировки (по умолчанию "date" по убыванию)
     * @return ResponseEntity со страницей отфильтрованных транзакций в формате DTO
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getFilteredTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<TransactionResponse> response = transactionService.getFilteredTransactions(
                accountId, type, category, startDate, pageable);

        return ResponseEntity.ok(response);
    }
}
