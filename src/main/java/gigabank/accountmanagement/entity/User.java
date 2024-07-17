package gigabank.accountmanagement.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static gigabank.accountmanagement.service.Utils.*;

/**
 * Информация о пользователе
 */
@Getter
@Setter
public class User {
    private String id = "";
    private String firstName = "";
    private String middleName = "";
    private String lastName = "";
    private LocalDate birthDate = UNKNOWN_DATE;
    private final List<BankAccount> bankAccounts = new ArrayList<>();

    public User(String firstName, String middleName, String lastName, LocalDate birthDate) {
        if (firstName != null & middleName != null & lastName != null & birthDate != null) {
            this.id = generateUserId();
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.birthDate = birthDate;
        }
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", bankAccounts=" + bankAccounts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;

        if (!Objects.equals(id, other.id)) return false;
        if (!Objects.equals(firstName, other.firstName)) return false;
        if (!Objects.equals(middleName, other.middleName)) return false;
        if (!Objects.equals(lastName, other.lastName)) return false;
        if (!Objects.equals(birthDate, other.birthDate)) return false;
        return Objects.equals(bankAccounts, other.bankAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, middleName, lastName, birthDate);
    }
}