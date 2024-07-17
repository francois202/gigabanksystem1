package gigabank.accountmanagement.service.Generate;

import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.service.TransactionService;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Data
@ToString(of = {"id", "value", "type", "category", "createdDate"})
public class TransactionTest{
    private String id;
    private BigDecimal value;
    private TransactionType type;
    private String category;
    private LocalDateTime createdDate;
    private GenerateTransactions generateTransactions = new GenerateTransactions();
    private Random random = new Random();

    public TransactionTest() {
        this.value = generateTransactions.generateValue();
        this.id = generateTransactions.generateId();
        this.type = generateTransactions.generateType(TransactionType.values());
        this.createdDate = generateTransactions.generateLocalDateTime(random);
        this.category = generateTransactions.generateCategory(TransactionService.transactionCategories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionTest that = (TransactionTest) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value) && type == that.type && Objects.equals(category, that.category) && Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, type, category, createdDate);
    }
}
