package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.request.AuthRequest;
import gigabank.accountmanagement.dto.request.TokenValidationRequest;
import gigabank.accountmanagement.dto.response.AuthResponse;
import gigabank.accountmanagement.dto.response.TokenValidationResponse;
import gigabank.accountmanagement.service.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jwt")
@RequiredArgsConstructor
public class JwtController {
    private final AuthService authService;

    /**
     * Аутентифицирует пользователя и возвращает JWT токен.
     *
     * @param request DTO с учетными данными пользователя
     * @return ResponseEntity с JWT токеном и информацией о пользователе
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Проверяет валидность JWT токена.
     *
     * @param request DTO с JWT токеном для проверки
     * @return ResponseEntity с результатом проверки токена
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@Valid @RequestBody
                                                                 TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validateToken(request));
    }


    /**
     * Предоставляет доступ к защищенному эндпоинту с JWT аутентификацией.
     *
     * @return ResponseEntity с подтверждением успешной аутентификации
     */
    @GetMapping("/protected-endpoint")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Проверка токена успешна");
    }
}