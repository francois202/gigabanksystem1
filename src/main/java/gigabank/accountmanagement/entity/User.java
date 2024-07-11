package gigabank.accountmanagement.entity;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<BankAccount> bankAccounts = new ArrayList<>();

     public User(String firstName, String middleName, String lastName, LocalDate birthDate) {
        this.id = generateUserId();
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    private static String generateUserId() {
        Random random = new Random();
        int randomId = random.nextInt(1, 1000);
        return String.valueOf(randomId);
    }
    /*
     * Переопределение методов hashCode() и equals() вручную.
     * Так как автоматические методы lombock вызывают рекурсию
     * при размещении в Map<User, List<BankAccount>>.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + id.hashCode();
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        String var10000 = this.getId();
        return "User(id=" + var10000 + ", firstName=" + this.getFirstName() + ", middleName=" + this.getMiddleName() + ", lastName=" + this.getLastName() + ", birthDate=" + this.getBirthDate() + ", bankAccounts=" + this.getBankAccounts().size() + ")";
    }
}
