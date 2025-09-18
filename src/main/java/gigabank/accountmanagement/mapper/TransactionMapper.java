package gigabank.accountmanagement.mapper;

import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.model.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "bankAccountEntity.id", target = "accountId")
    @Mapping(source = "bankAccountEntity.owner.name", target = "accountOwnerName")
    TransactionResponse toResponse(TransactionEntity transaction);
}
