package gigabank.accountmanagement.controllers;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable("id") int id) {
        BankAccount account = bankAccountService.findAccountById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/new")
    public ResponseEntity<BankAccount> createAccount(@RequestBody User user) {
        BankAccount account = new BankAccount();
        account.setOwner(user);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable("id") int id, @RequestBody BigDecimal amount) {
        BankAccount account = bankAccountService.findAccountById(id);
        account.setBalance(account.getBalance().add(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable("id") int id, @RequestBody BigDecimal amount) {
        BankAccount account = bankAccountService.findAccountById(id);
        bankAccountService.withdraw(account, amount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> transactionHistory(@PathVariable("id") int id) {
        BankAccount account = bankAccountService.findAccountById(id);
        List<Transaction> transactionList = account.getTransactions();
        return ResponseEntity.ok(transactionList);
    }
}
