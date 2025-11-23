package org.example.controller;

import org.example.dto.AccountDTO;
import org.example.model.AccountEntity;
import org.example.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable Long id) {
        return accountRepository.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public AccountDTO createAccount(@RequestBody AccountDTO account) {
        AccountEntity accountEntity = toEntity(account);
        return toDTO(accountRepository.save(accountEntity));
    }

    private AccountDTO toDTO(AccountEntity entity) {
        AccountDTO dto = new AccountDTO();
        dto.setId(entity.getId());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setBalance(entity.getBalance());
        dto.setOwnerId(entity.getOwnerId());
        dto.setOwnerName(entity.getOwnerName());
        dto.setOwnerEmail(entity.getOwnerEmail());
        dto.setBlocked(entity.isBlocked());
        return dto;
    }

    private AccountEntity toEntity(AccountDTO dto) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setBalance(dto.getBalance());
        entity.setOwnerId(dto.getOwnerId());
        entity.setOwnerName(dto.getOwnerName());
        entity.setOwnerEmail(dto.getOwnerEmail());
        entity.setBlocked(dto.isBlocked());
        return entity;
    }
}
