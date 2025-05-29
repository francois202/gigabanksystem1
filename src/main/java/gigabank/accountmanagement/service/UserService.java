package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.model.CreateUserDto;
import gigabank.accountmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(CreateUserDto userDto) {
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setMiddleName(userDto.getMiddleName());
        user.setLastName(userDto.getLastName());
        user.setBirthDate(userDto.getBirthDate());
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());

        return userRepository.save(user);
    }
}