package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.model.UserEntity;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    public UserResponse(UserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.phoneNumber = entity.getPhoneNumber();
    }
}
