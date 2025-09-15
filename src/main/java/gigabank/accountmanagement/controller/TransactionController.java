package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.request.TransferRequest;
import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.service.TransactionService;
import gigabank.accountmanagement.service.payment.strategies.BankTransferStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    private final BankTransferStrategy bankTransferStrategy;

    public TransactionController(TransactionService transactionService,
                                 BankTransferStrategy bankTransferStrategy) {
        this.transactionService = transactionService;
        this.bankTransferStrategy = bankTransferStrategy;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        bankTransferStrategy.process(
                request.getAccount(),
                request.getAmount(),
                request.getDetails()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getFilteredTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
            Page<TransactionEntity> transactions = transactionService.getFilteredTransactions(
                    accountId, type, category, startDate, pageable);

            Page<TransactionResponse> response = transactions.map(TransactionResponse::new);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting transactions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
