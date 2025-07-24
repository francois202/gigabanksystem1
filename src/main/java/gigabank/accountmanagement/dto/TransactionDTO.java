package gigabank.accountmanagement.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TransactionDTO {
    private String id;
    private String userId;
    private BigDecimal amount;
    private String type;
    private Timestamp date;
    private String source;
    private String target;

    public TransactionDTO(String id, String userId, BigDecimal amount, String type, Timestamp date, String source, String target) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.source = source;
        this.target = target;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
