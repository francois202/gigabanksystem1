package gigabank.accountmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация о пользователе
 */
@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@ToString(exclude = "bankAccountEntities")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @Column(name = "phone", nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccountEntity> bankAccountEntities = new ArrayList<>();
}
