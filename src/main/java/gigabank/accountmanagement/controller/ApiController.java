package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.model.JwtResponse;
import gigabank.accountmanagement.model.LoginRequest;
import gigabank.accountmanagement.security.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Это публичный endpoint, доступен без аутентификации";
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        return "Это защищенный endpoint, требуется Basic Auth";
    }

    @PostMapping("/public/token")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtTokenUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @GetMapping("/protected-endpoint")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Проверка токена успешна");
    }
}
