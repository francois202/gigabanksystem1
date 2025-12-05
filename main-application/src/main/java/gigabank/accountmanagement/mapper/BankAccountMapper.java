package gigabank.accountmanagement.mapper;

import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.model.BankAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.name", target = "ownerName")
    @Mapping(source = "owner.email", target = "ownerEmail")
    @Mapping(target = "transactions", ignore = true)
    BankAccountResponse toResponse(BankAccountEntity entity);
}
