package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.model.TransferRequest;
import gigabank.accountmanagement.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService accountService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestBody TransferRequest request
    ) {
        accountService.transfer(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount()
        );
        return ResponseEntity.ok("Transfer successful");
    }
}