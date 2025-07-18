package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.dto.TransferRequest;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final BankAccountService bankAccountService;

    @Autowired
    public TransactionController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferRequest request) {
        bankAccountService.transfer(request.getFromAccountId(), request.getToAccountId(),
                request.getAmount(), request.getDescription());
        return ResponseEntity.ok().build();
    }
}
