package gigabank.accountmanagement.service.controllers;

import gigabank.accountmanagement.dto.AmountRequest;
import gigabank.accountmanagement.dto.CreateAccountRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.exceptions.AccountNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping
    public BankAccount createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return bankAccountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public BankAccount getAccount(@PathVariable String id) {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            throw new AccountNotFoundException("Аккаунт с ID " + id + " не найден");
        }
        return account;
    }

    @PostMapping("/{id}/deposit")
    public BankAccount deposit(@PathVariable String id, @Valid @RequestBody AmountRequest request) throws SQLException {
        bankAccountService.deposit(id, request.amount());
        return bankAccountService.getAccount(id);
    }

    @PostMapping("/{id}/withdraw")
    public BankAccount withdraw(@PathVariable String id, @Valid @RequestBody AmountRequest request) throws SQLException {
        BankAccount account = bankAccountService.getAccount(id);
        if (account == null) {
            throw new AccountNotFoundException("Аккаунт с ID " + id + " не найден");
        }
        bankAccountService.withdraw(id, request.amount());
        return bankAccountService.getAccount(id);
    }
}