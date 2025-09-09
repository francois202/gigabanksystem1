package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.dto.DepositWithdrawRequest;
import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {
    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountEntity> getAccount(@PathVariable Integer id) {
        BankAccountEntity accountById = bankAccountService.findAccountById(id);
        if (accountById == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accountById);
    }

    @PostMapping
    public ResponseEntity<BankAccountEntity> createAccount() {
        BankAccountEntity accountCreated = BankAccountService.createTestAccount();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountCreated);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable Integer id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        bankAccountService.deposit(bankAccountService.findAccountById(id), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Integer id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        bankAccountService.withdraw(bankAccountService.findAccountById(id), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionEntity>> getTransactions(@PathVariable Integer id) {
        List<TransactionEntity> transactionEntities = bankAccountService.findAccountById(id).getTransactionEntities();
        if (transactionEntities == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactionEntities);
    }

}
