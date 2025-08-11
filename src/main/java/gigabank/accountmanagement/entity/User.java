package gigabank.accountmanagement.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Информация о пользователе
 */
@Data
public class User {
    private String id;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;
    private List<BankAccount> bankAccounts = new ArrayList<>();
}
