package gigabank.accountmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;
}