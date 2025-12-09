package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.request.UserUpdateRequest;
import gigabank.accountmanagement.dto.response.UserAccountResponse;
import gigabank.accountmanagement.exception.UserNotFoundException;
import gigabank.accountmanagement.mapper.UserMapper;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис отвечает за управление данными пользователей.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Получает информацию о пользователе
     *
     * @param email email пользователя
     * @return пользователя в формате DTO
     */
    @Cacheable(value = "usersCache", key = "#email")
    public UserAccountResponse getUserAccountByEmail(String email) {
        log.info("Попытка поиска пользователя по email: {}", email);
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        log.info("Пользователь найден. email: {}", email);
        return userMapper.toResponse(userEntity);
    }

    /**
     * Обновляет информацию о пользователе
     * @param email email пользователя
     * @param request DTO с обновленным данными пользователя
     * @return обновленный DTO пользователя
     */
    @Transactional
    @CacheEvict(value = "usersCache", key = "#email")
    public UserAccountResponse updateUser(String email, UserUpdateRequest request) {
        log.info("Обновление пользователя с email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());

        UserEntity updatedUser = userRepository.save(user);
        log.info("Пользователь обновлен: {}", email);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Удаляет информацию о пользователе
     * @param email email пользователя
     */
    @Transactional
    @CacheEvict(value = "usersCache", key = "#email")
    public void deleteUser(String email) {
        log.info("Удаление пользователя с email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        userRepository.delete(user);
        log.info("Пользователь удален: {}", email);
    }
}
