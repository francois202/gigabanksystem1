package gigabank.accountmanagement.mapper;

import gigabank.accountmanagement.dto.response.UserAccountResponse;
import gigabank.accountmanagement.model.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserAccountResponse toResponse(UserEntity userEntity);
}
