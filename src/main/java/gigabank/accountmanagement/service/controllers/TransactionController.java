package gigabank.accountmanagement.service.controllers;

import gigabank.accountmanagement.dto.TransferRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final BankAccountService bankAccountService;

    @Autowired
    public TransactionController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        BankAccount fromAccount = bankAccountService.getAccount(request.fromId());
        BankAccount toAccount = bankAccountService.getAccount(request.toId());

        if (fromAccount == null) {
            return ResponseEntity.badRequest().body("Счёт отправителя не найден");
        }
        if (toAccount == null) {
            return ResponseEntity.badRequest().body("Счёт получателя не найден");
        }
        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            return ResponseEntity.badRequest().body("Недостаточно средств");
        }

        bankAccountService.transfer(request.fromId(), request.toId(), request.amount());
        return ResponseEntity.ok("Перевод выполнен успешно");
    }
    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String id) {
        List<Transaction> transactions = bankAccountService.getTransactions(id);
        if (transactions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactions);
    }
}