package week_one;

import lombok.Data;
import week_two.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data

public class Transaction {
    private String id;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime date;
    private BankAccount sourceAccount;
    private BankAccount targetAccount;
    private BigDecimal value;
    private String category;

    public Transaction(BigDecimal amount, TransactionType type, BankAccount source, BankAccount target) {
        this.id = UUID.randomUUID().toString(); // задаю рандомный айдишник и поэтому не прописываю его в параметрах
        this.amount = amount;
        this.type = type;
        this.date = LocalDateTime.now(); // задаю значение времени и даты в ту секунду когда вызывается транзакция
        this.sourceAccount = source;
        this.targetAccount = target;


    }

    public Transaction(String id, BigDecimal amount, TransactionType type, String category, LocalDateTime date) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;

    }


    public String toString() { // здесь с помощью toString я решаю что будет выводиться при вызове getTransaction
        return type + " вот стока " + amount + "вот во стока " + date;
    }


}
