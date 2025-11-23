package org.example.client.controller;

import org.example.client.dto.Account;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/client/accounts")
public class AccountClientController {
    private final WebClient webClient;

    public AccountClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/{id}")
    public Mono<Account> getAccount(@PathVariable Long id) {
        return webClient.get()
                .uri("/api/accounts/{id}", id)
                .retrieve()
                .bodyToMono(Account.class);
    }

    @PostMapping
    public Mono<Account> createAccount(@RequestBody Account account) {
        return webClient.post()
                .uri("/api/accounts")
                .bodyValue(account)
                .retrieve()
                .bodyToMono(Account.class);
    }

}
