package gigabank.accountmanagement.service.controllers;

import gigabank.accountmanagement.dto.AmountRequest;
import gigabank.accountmanagement.dto.CreateAccountRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.exceptions.AccountNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            BankAccount createdAccount = bankAccountService.createAccount(request);
            return ResponseEntity.ok(createdAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable String id) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<BankAccount> deposit(@PathVariable String id, @Valid @RequestBody AmountRequest request) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            throw new AccountNotFoundException("Аккаунт с ID " + id + " не найден");
        }
        bankAccountService.deposit(id, request.amount());
        return ResponseEntity.ok(bankAccountService.getAccount(id));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<BankAccount> withdraw(@PathVariable String id, @Valid @RequestBody AmountRequest request) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            throw new AccountNotFoundException("Аккаунт с ID " + id + " не найден");
        }
        bankAccountService.withdraw(id, request.amount());
        return ResponseEntity.ok(bankAccountService.getAccount(id));
    }
}