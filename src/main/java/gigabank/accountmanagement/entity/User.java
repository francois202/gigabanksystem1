package gigabank.accountmanagement.entity;


import java.time.LocalDate;
import java.util.List;

import java.time.LocalDate;
import java.util.List;

// Информация о пользователе

public class User {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private List<BankAccount> bankAccounts;

    public List<BankAccount> getBankAccounts(){
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccount> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }
}
