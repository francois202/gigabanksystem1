package gigabank.accountmanagement.service.Generate;

import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

public interface GenerateTransactionsInfo {
    String generateId();
    BigDecimal generateValue();
    TransactionType generateType(TransactionType[] transactionType);
    String generateCategory(Set<String> transactionCategories);
    LocalDateTime generateLocalDateTime(Random random);
}
