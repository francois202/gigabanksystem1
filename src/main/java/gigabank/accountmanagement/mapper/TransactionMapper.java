package gigabank.accountmanagement.mapper;

import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.dto.request.TransactionGenerateRequest;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.model.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "bankAccountEntity.id", target = "accountId")
    @Mapping(source = "bankAccountEntity.owner.name", target = "accountOwnerName")
    TransactionResponse toResponse(TransactionEntity transaction);

    @Mapping(source = "bankAccountEntity.id", target = "bankAccountId")
    TransactionMessage toMessage(TransactionEntity transaction);

    @Mapping(source = "bankAccountId", target = "bankAccountEntity.id")
    @Mapping(target = "bankAccountEntity", ignore = true)
    TransactionEntity toEntity(TransactionMessage message);

    @Mapping(source = "accountId", target = "bankAccountEntity.id")
    @Mapping(target = "bankAccountEntity", ignore = true)
    @Mapping(target = "id", source = "transactionId")
    @Mapping(target = "type", source = "transactionType")
    @Mapping(target = "value", source = "amount")
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "sourceAccount", ignore = true)
    @Mapping(target = "targetAccount", ignore = true)
    @Mapping(target = "category", ignore = true)
    TransactionEntity toEntity(TransactionGenerateRequest request);
}
