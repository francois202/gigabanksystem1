package gigabank.accountmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Контроллер для предоставления информации о API
 */
@RestController
public class ApiInfoController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = Map.of(
            "service", "GigaBank Core API",
            "version", "v1.0.0",
            "description", "Banking system REST API for account and transaction management",
            "timestamp", Instant.now(),
            "documentation", "/swagger-ui.html",
            "health", "/actuator/health",
            "endpoints", Map.of(
                "accounts", Map.of(
                    "list", "GET /api/accounts?page=0&size=10",
                    "get", "GET /api/account-actions/{id}",
                    "create", "POST /api/account-actions/create",
                    "deposit", "POST /api/account-actions/{id}/deposit",
                    "withdraw", "POST /api/account-actions/{id}/withdraw",
                    "block", "POST /api/account-actions/{id}/block",
                    "delete", "DELETE /api/accounts/{id}"
                ),
                "transactions", Map.of(
                    "list", "GET /api/transactions?accountId={id}&type={type}&category={cat}",
                    "transfer", "POST /api/transactions/transfer"
                )
            )
        );
        return ResponseEntity.ok(info);
    }
}
