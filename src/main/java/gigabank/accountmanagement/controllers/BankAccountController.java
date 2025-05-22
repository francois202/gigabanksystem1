package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.dto.DepositWithdrawRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class BankAccountController {
    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable Integer id) {
        BankAccount account = bankAccountService.findAccountById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable Integer id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        bankAccountService.deposit(id, request.getAmount(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Integer id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        bankAccountService.withdraw(id, request.getAmount(), request.getDescription());
        return ResponseEntity.ok().build();
    }

}