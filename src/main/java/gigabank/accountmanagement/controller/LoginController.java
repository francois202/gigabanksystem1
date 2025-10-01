package gigabank.accountmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    /**
     * Возвращает страницу входа в систему.
     *
     * @return имя HTML шаблона страницы входа
     */
    @GetMapping("/auth/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Возвращает главную панель управления после успешной аутентификации.
     *
     * @return имя HTML шаблона панели управления
     */
    @GetMapping("/form/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
