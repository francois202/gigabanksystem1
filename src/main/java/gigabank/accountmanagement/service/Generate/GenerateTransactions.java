package gigabank.accountmanagement.service.Generate;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GenerateTransactions implements GenerateTransactionsInfo {
    private Random random = new Random();
    private static List<TransactionTest> transactionList = new ArrayList<>();

    @Override
    public String generateId() {
        return String.valueOf(random.nextInt(10_000));
    }

    @Override
    public BigDecimal generateValue() {
        return BigDecimal.valueOf(random.nextInt(10_000));
    }

    @Override
    public TransactionType generateType(TransactionType[] transactionType) {
        return transactionType[random.nextInt(transactionType.length)];
    }

    @Override
    public String generateCategory(Set<String> transactionCategories) {
        return transactionCategories.toArray()[random.nextInt(transactionCategories.size())].toString();
    }

    @Override
    public LocalDateTime generateLocalDateTime(Random random) {
        int i = random.nextInt(4);

        if (i == 1) {
            return LocalDateTime.now().minusDays(1L);
        } else if (i == 2) {
            return LocalDateTime.now().minusWeeks(2L);
        } else if (i == 3) {
            return LocalDateTime.now().minusMonths(1L);
        } else
            return LocalDateTime.now();
    }

}
