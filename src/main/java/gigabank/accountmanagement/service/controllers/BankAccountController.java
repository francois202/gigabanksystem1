package gigabank.accountmanagement.service.controllers;

import gigabank.accountmanagement.dto.AmountRequest;
import gigabank.accountmanagement.dto.CreateAccountRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }
    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        User user = new User();
        user.setId(request.getUserId());
        BankAccount account = new BankAccount("ACC_" + System.currentTimeMillis(), new ArrayList<>());
        account.setOwner(user);
        account.setBalance(request.getInitialBalance());
        BankAccount createdAccount = bankAccountService.createAccount(account);
        return ResponseEntity.ok((createdAccount));
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
    public ResponseEntity<BankAccount> deposit(@PathVariable String id,@Valid @RequestBody AmountRequest request) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        bankAccountService.deposit(id, request.getAmount());
        return ResponseEntity.ok(account);
    }
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<BankAccount> withdraw(@PathVariable String id,@Valid @RequestBody AmountRequest request) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null){
            return ResponseEntity.notFound().build();
        }
        bankAccountService.withdraw(id, request.getAmount());
        return ResponseEntity.ok(account);
    }
}