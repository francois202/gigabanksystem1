package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления счетами
 */
@RestController
@RequestMapping("/api/account-actions")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final TransactionService transactionService;

    /**
     * Получает полную информацию о счете по его идентификатору.
     *
     * @param id уникальный идентификатор счета
     * @return DTO с информацией о счете
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BankAccountResponse getAccount(@PathVariable Long id) {
        return bankAccountService.getAccountById(id);
    }

    /**
     * Создает новый тестовый банковский счет.
     *
     * @return ResponseEntity с созданным DTO счета
     */
    @PostMapping("/create")
    public ResponseEntity<BankAccountResponse> createAccount() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.createTestAccount());
    }

    /**
     * Пополняет баланс счета на указанную сумму.
     *
     * @param id уникальный идентификатор счета
     * @param request DTO с данными для пополнения счета
     * @return ResponseEntity с обновленным DTO счета
     */
    @PostMapping("/{id}/deposit")
    public ResponseEntity<BankAccountResponse> deposit(
            @PathVariable Long id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(bankAccountService.deposit(id, request));
    }

    /**
     * Снимает указанную сумму со счета.
     *
     * @param id уникальный идентификатор счета
     * @param request DTO с данными для снятия средств
     * @return ResponseEntity с обновленным DTO счета
     */
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<BankAccountResponse> withdraw(
            @PathVariable Long id,
            @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(bankAccountService.withdraw(id, request));
    }

    /**
     * Получает список всех транзакций по указанному счету.
     *
     * @param id уникальный идентификатор счета
     * @return ResponseEntity со списком DTO транзакций
     */
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(id));
    }

    /**
     * Получает страницу со всеми банковскими счетами с пагинацией.
     *
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @return ResponseEntity со страницей DTO счетов
     */
    @GetMapping
    public ResponseEntity<Page<BankAccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bankAccountService.getAllAccounts(pageable));
    }

    /**
     * Закрывает указанный банковский счет.
     *
     * @param id уникальный идентификатор счета
     * @return ResponseEntity с пустым телом
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        bankAccountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Блокирует или разблокирует указанный банковский счет.
     *
     * @param id уникальный идентификатор счета
     * @return ResponseEntity с обновленным DTO счета
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<BankAccountResponse> toggleAccountBlock(@PathVariable Long id) {
        return ResponseEntity.ok(bankAccountService.toggleAccountBlock(id));
    }
}
