package gigabank.accountmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
