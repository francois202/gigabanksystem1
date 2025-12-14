package gigabank.accountmanagement.service.security;

import gigabank.accountmanagement.dto.request.AuthRequest;
import gigabank.accountmanagement.dto.request.TokenValidationRequest;
import gigabank.accountmanagement.dto.response.AuthResponse;
import gigabank.accountmanagement.dto.response.TokenValidationResponse;
import gigabank.accountmanagement.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Аутентифицирует пользователя по учетным данным и генерирует JWT токен.
     *
     * @param authRequest DTO с именем пользователя и паролем для аутентификации
     * @return AuthResponse с JWT токеном, именем пользователя и сообщением об успехе
     * @throws AuthenticationException если аутентификация не удалась или произошла ошибка сервиса
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            log.info("Attempting authentication for user: {}", authRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (!authentication.isAuthenticated()) {
                log.warn("Authentication failed for user: {}", authRequest.getUsername());
                throw new AuthenticationException("Authentication failed");
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);

            log.info("Authentication successful for user: {}", userDetails.getUsername());

            return new AuthResponse(jwtToken, userDetails.getUsername(), "Authentication successful");
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}", authRequest.getUsername(), e);
            throw new AuthenticationException("Authentication service error", e);
        }
    }

    /**
     * Проверяет валидность JWT токена и возвращает результат проверки.
     *
     * @param validationRequest DTO с JWT токеном для валидации
     * @return TokenValidationResponse с флагом валидности, именем пользователя и сообщением об ошибке
     */
    public TokenValidationResponse validateToken(TokenValidationRequest validationRequest) {
        String token = validationRequest.getToken();

        try {
            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isValid = jwtService.isTokenValid(token, userDetails);

            log.info("Token validation result for user {}: {}", username, isValid);

            return new TokenValidationResponse(isValid, username, null);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return new TokenValidationResponse(false, null, "Invalid token");
        }
    }
}
