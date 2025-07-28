package gigabank.accountmanagement.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// информация о банковском аккаунте
public class BankAccount {
    private String id;
    private BigDecimal balance;
    private User owner;
    private List<Transaction> transactions;

    public BankAccount(String generatedId){
        this.id = generatedId;
        this.transactions = new ArrayList<>();
    }
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
