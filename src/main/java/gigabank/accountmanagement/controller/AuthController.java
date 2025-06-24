package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.model.JwtResponse;
import gigabank.accountmanagement.security.JwtTokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/oauth-success")
    public ResponseEntity<JwtResponse> oauthSuccess(
            @AuthenticationPrincipal OAuth2User oauthUser,
            JwtTokenUtils jwtTokenUtils) {

        String jwt = jwtTokenUtils.generateOAuthToken(oauthUser);
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}