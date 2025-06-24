package gigabank.accountmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "home"; // Главная страница (публичная)
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Страница входа
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Защищённая страница
    }
}