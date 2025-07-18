package gigabank.accountmanagement.dto;

import java.math.BigDecimal;

public class BankAccountDTO {
    private String id;
    private String number;
    private String userId;
    private BigDecimal balance;

    public BankAccountDTO(String id, String number, String userId, BigDecimal balance) {
        this.id = id;
        this.number = number;
        this.userId = userId;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = String.valueOf(number);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
