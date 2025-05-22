package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.dto.TransferRequest;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final BankAccountService bankAccountService;

    public TransactionController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        bankAccountService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok().build();
    }
}