package gigabank.accountmanagement.service.security;

import gigabank.accountmanagement.dto.OAuth2UserDTO;
import gigabank.accountmanagement.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final JwtService jwtService;

    /**
     * Загружает информацию о пользователе из OAuth2 провайдера.
     *
     * @param userRequest запрос OAuth2 с информацией о клиенте и токене доступа
     * @return OAuth2User с атрибутами пользователя от OAuth2 провайдера
     * @throws OAuth2AuthenticationException если не удалось загрузить информацию о пользователе
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserDTO userDTO = processOAuth2User(registrationId, oAuth2User.getAttributes());

        log.info("OAuth2 login successful for user: {} from provider: {}", userDTO.getEmail(), registrationId);

        return oAuth2User;
    }

    /**
     * Обрабатывает атрибуты пользователя из OAuth2 провайдера и создает DTO.
     *
     * @param provider идентификатор OAuth2 провайдера (google, github)
     * @param attributes атрибуты пользователя, полученные от OAuth2 провайдера
     * @return OAuth2UserDTO с обработанной информацией о пользователе
     */
    private OAuth2UserDTO processOAuth2User(String provider, Map<String, Object> attributes) {
        OAuth2UserDTO userDTO = new OAuth2UserDTO();
        userDTO.setProvider(provider);
        userDTO.setAttributes(attributes);

        if ("google".equals(provider)) {
            userDTO.setEmail((String) attributes.get("email"));
            userDTO.setName((String) attributes.get("name"));
            userDTO.setProviderId((String) attributes.get("sub"));
        } else if ("github".equals(provider)) {
            userDTO.setName((String) attributes.get("name"));
            userDTO.setEmail((String) attributes.get("email"));
            userDTO.setProviderId(String.valueOf(attributes.get("id")));

            if (userDTO.getEmail() == null) {
                userDTO.setEmail(userDTO.getProviderId() + "@github");
            }
        }

        return userDTO;
    }

    /**
     * Обрабатывает успешную OAuth2 аутентификацию и генерирует JWT токен.
     *
     * @param oauth2User аутентифицированный пользователь OAuth2
     * @return AuthResponse с JWT токеном, именем пользователя и сообщением об успехе
     * @throws OAuth2AuthenticationException если обработка аутентификации не удалась
     */
    public AuthResponse processOAuth2Success(OAuth2User oauth2User) {
        try {
            String jwtToken = generateOAuth2JwtToken(oauth2User);
            String email = oauth2User.getAttribute("email");
            String username = email != null ? email : oauth2User.getName();

            log.info("OAuth2 authentication successful for user: {}", username);

            return new AuthResponse(
                    jwtToken,
                    username,
                    "OAuth2 authentication successful"
            );

        } catch (Exception e) {
            log.error("OAuth2 authentication processing failed", e);
            throw new OAuth2AuthenticationException("OAuth2 authentication failed");
        }
    }

    /**
     * Генерирует JWT токен для пользователя, аутентифицированного через OAuth2.
     *
     * @param oAuth2User пользователь OAuth2 с атрибутами от провайдера
     * @return строка с JWT токеном для доступа к защищенным ресурсам
     */
    public String generateOAuth2JwtToken(OAuth2User oAuth2User) {
        String username = oAuth2User.getAttribute("email");
        if (username == null) {
            username = oAuth2User.getName();
        }

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(username)
                        .password("")
                        .authorities("ROLE_OAUTH2_USER")
                        .build();

        return jwtService.generateToken(userDetails);
    }
}
