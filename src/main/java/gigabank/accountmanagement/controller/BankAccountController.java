package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.service.BankAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccount(@PathVariable Long id) {
        BankAccountEntity account = bankAccountService.findAccountById(id);
        BankAccountResponse response = new BankAccountResponse(account);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<BankAccountResponse> createAccount() {
        BankAccountEntity accountCreated = BankAccountService.createTestAccount();
        BankAccountResponse response = new BankAccountResponse(accountCreated);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositWithdrawRequest request) {

        bankAccountService.deposit(id, request.getAmount());
        return ResponseEntity.ok().build();

    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody DepositWithdrawRequest request) {

        bankAccountService.withdraw(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long id) {
        BankAccountEntity account = bankAccountService.findAccountById(id);
        List<TransactionResponse> response = account.getTransactionEntities().stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BankAccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccountEntity> accountsPage = bankAccountService.getAllAccounts(pageable);

        Page<BankAccountResponse> responsePage = accountsPage.map(BankAccountResponse::new);
        return ResponseEntity.ok(responsePage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        bankAccountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> toggleAccountBlock(@PathVariable Long id) {
        BankAccountEntity account = bankAccountService.toggleAccountBlock(id);
        BankAccountResponse response = new BankAccountResponse(account);
        return ResponseEntity.ok(response);
    }
}
