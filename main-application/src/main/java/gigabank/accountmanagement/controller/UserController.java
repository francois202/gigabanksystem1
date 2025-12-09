package gigabank.accountmanagement.controller;

import gigabank.accountmanagement.dto.request.UserUpdateRequest;
import gigabank.accountmanagement.dto.response.UserAccountResponse;
import gigabank.accountmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления пользователями
 */
@RestController
@RequestMapping("/api/user-actions")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Получает информацию о пользователе
     * @param email email пользователя
     * @return DTO с информацией о пользователе
     */
    @GetMapping("/{email}")
    @ResponseStatus(HttpStatus.OK)
    public UserAccountResponse getUserAccountByEmail(@PathVariable String email) {
        return userService.getUserAccountByEmail(email);
    }

    /**
     * Обновляет информацию о пользователе
     * @param email email пользователя
     * @return ResponseEntity с обновленным DTO пользователя
     */
    @PutMapping("/{email}")
    public ResponseEntity<UserAccountResponse> updateUser(
            @PathVariable String email,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(email, request));
    }

    /**
     * Удаляет информацию о пользователе
     * @param email email пользователя
     * @return ResponseEntity без контента
     */    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
