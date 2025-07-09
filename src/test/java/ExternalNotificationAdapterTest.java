import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationAdapter;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalNotificationAdapterTest {

    private ExternalNotificationService externalNotificationService;
    private ExternalNotificationAdapter adapter;
    private User user;
    private String message;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        externalNotificationService = new ExternalNotificationService();
        adapter = new ExternalNotificationAdapter(externalNotificationService);
        user = new User();
        message = "Тестовое уведомление";
        System.setOut(new PrintStream(outContent));
    }
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }
    @Test
    @DisplayName("Проверяет отправку SMS при наличии номера телефона")
    void testSendPaymentNotificationSendsSmsWhenPhoneIsProvided() {
        user.setPhoneNumber("1234567890");
        user.setEmail(null);
        adapter.sendPaymentNotification(user, message);
        System.err.println("Фактический вывод: [" + outContent.toString() + "]");
        String expectedOutput = String.format("Отправка SMS от unknown на 1234567890: Тестовое уведомление%s", System.lineSeparator());
        assertEquals(expectedOutput, outContent.toString(), "SMS-сообщение должно быть отправлено с правильным сообщением");
    }
    @Test
    @DisplayName("Проверяет отправку email при наличии адреса электронной почты")
    void testSendPaymentNotificationSendsEmailWhenEmailIsProvided() {
        user.setPhoneNumber(null);
        user.setEmail("test@example.com");
        adapter.sendPaymentNotification(user, message);
        System.err.println("Фактический вывод: [" + outContent.toString() + "]");
        String expectedOutput = String.format("Отправка Email от unknown на test@example.com: Уведомление об оплате - Тестовое уведомление%s", System.lineSeparator());
        assertEquals(expectedOutput, outContent.toString(), "Электронное письмо должно быть отправлено с правильной темой и сообщением");
    }
}