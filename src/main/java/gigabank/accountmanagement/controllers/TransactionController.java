package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.dto.TransferRequest;
import gigabank.accountmanagement.service.payment.strategies.BankTransferStrategy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final BankTransferStrategy bankTransferStrategy;

    public TransactionController(BankTransferStrategy bankTransferStrategy) {
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
}
