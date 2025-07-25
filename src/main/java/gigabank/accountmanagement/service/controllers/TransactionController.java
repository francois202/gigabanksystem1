package gigabank.accountmanagement.service.controllers;

import gigabank.accountmanagement.dto.TransferRequest;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.service.BankAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final BankAccountService bankAccountService;

    public TransactionController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) throws SQLException {
        bankAccountService.transfer(request.fromId(), request.toId(), request.amount());
        return ResponseEntity.ok().build();
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