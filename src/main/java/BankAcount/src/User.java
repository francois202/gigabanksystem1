package BankAcount.src;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    @Getter
    private String name;
    @Getter
    private List<BankAccount> accounts;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
        this.accounts = new ArrayList<>(); // создаю пустой список пользователей
    }

    public void addAccount(BankAccount account) {
        accounts.add(account); // add прибавляет accounT в accountS
    }

    public void getAccounts(BankAccount account) {
        accounts.add(account);
    }

}

