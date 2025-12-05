package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.response.AuthResponse;
import gigabank.accountmanagement.service.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final CustomOAuth2UserService oauth2UserService;

    /**
     * Обрабатывает успешную OAuth2 аутентификацию и возвращает JWT токен.
     *
     * @param oauth2User аутентифицированный пользователь OAuth2
     * @return ResponseEntity с JWT токеном и информацией о пользователе
     */
    @GetMapping("/success")
    public ResponseEntity<AuthResponse> oauth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        return ResponseEntity.ok(oauth2UserService.processOAuth2Success(oauth2User));
    }

    /**
     * Предоставляет доступ к защищенному эндпоинту с OAuth2 аутентификацией.
     *
     * @return ResponseEntity с подтверждением успешной OAuth2 аутентификации
     */
    @GetMapping("/protected-endpoint")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Проверка токена OAuth2.0 успешна");
    }
}