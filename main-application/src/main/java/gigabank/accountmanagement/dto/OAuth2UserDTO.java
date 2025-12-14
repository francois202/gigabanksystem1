package gigabank.accountmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2UserDTO {
    private String name;
    private String email;
    private String provider;
    private String providerId;
    private Map<String, Object> attributes;
}
