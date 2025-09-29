package gigabank.accountmanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    /**
     * Возвращает дэшборд с базовой аутентификацией
     *
     * @return ResponseEntity с информацией о доступе до дэшборда
     */
    @GetMapping("/basic/dashboard")
    public ResponseEntity<String> basicDashboard() {
        return ResponseEntity.ok("Basic Auth Dashboard - Access granted");
    }

    /**
     * Предоставляет доступ к админским функциям.
     *
     * @return ResponseEntity с подтверждением админского доступа
     */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Admin access granted");
    }

    /**
     * Предоставляет доступ к пользовательскому профилю.
     *
     * @return ResponseEntity с подтверждением пользовательского доступа
     */
    @GetMapping("/api/user-profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("User access granted");
    }
}
