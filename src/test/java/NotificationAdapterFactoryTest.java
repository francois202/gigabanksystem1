import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationAdapterFactoryTest {

    private NotificationAdapterFactory factory;
    private ExternalNotificationService externalNotificationService;

    @BeforeEach
    void setUp() {
        // Мокаем ExternalNotificationService
        externalNotificationService = Mockito.mock(ExternalNotificationService.class);
        // Создаём фабрику с мокированным сервисом и тестовым типом
        factory = new NotificationAdapterFactory(externalNotificationService, "email");
    }

    @Test
    @DisplayName("Проверяет, что при notificationType = \"email\" возвращается EmailNotificationAdapter")
    void shouldReturnEmailNotificationAdapterWhenTypeIsEmail() {
        // Устанавливаем тип уведомления
        factory = new NotificationAdapterFactory(externalNotificationService, "email");

        NotificationAdapter adapter = factory.getNotificationAdapter();

        assertTrue(adapter instanceof EmailNotificationAdapter);
        verifyNoInteractions(externalNotificationService); // Проверяем, что сервис не использовался
    }

    @Test
    @DisplayName("Проверяет, что при notificationType = \"sms\" возвращается SmsNotificationAdapter")
    void shouldReturnSmsNotificationAdapterWhenTypeIsSms() {
        factory = new NotificationAdapterFactory(externalNotificationService, "sms");

        NotificationAdapter adapter = factory.getNotificationAdapter();

        assertTrue(adapter instanceof SmsNotificationAdapter);
        verifyNoInteractions(externalNotificationService);
    }

    @Test
    @DisplayName("Проверяет, что при notificationType = \"external\" возвращается ExternalNotificationAdapter")
    void shouldReturnExternalNotificationAdapterWhenTypeIsExternal() {
        factory = new NotificationAdapterFactory(externalNotificationService, "external");

        NotificationAdapter adapter = factory.getNotificationAdapter();

        assertTrue(adapter instanceof ExternalNotificationAdapter);
        verifyNoInteractions(externalNotificationService);
    }

    @Test
    @DisplayName("Проверяет, что при неизвестном типе выбрасывается исключение с правильным сообщением")
    void shouldThrowIllegalArgumentExceptionWhenTypeIsUnknown() {
        factory = new NotificationAdapterFactory(externalNotificationService, "unknown");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                factory::getNotificationAdapter
        );

        assertEquals("Неизвестный тип уведомления: unknown", exception.getMessage());
        verifyNoInteractions(externalNotificationService);
    }

    @Test
    @DisplayName("Убеждается, что выбор адаптера не зависит от регистра")
    void shouldBeCaseInsensitiveWhenChoosingAdapter() {
        factory = new NotificationAdapterFactory(externalNotificationService, "EMAIL");

        NotificationAdapter adapter = factory.getNotificationAdapter();

        assertTrue(adapter instanceof EmailNotificationAdapter);
        verifyNoInteractions(externalNotificationService);
    }

    @Test
    @DisplayName("Проверяет, что инжектированный ExternalNotificationService корректно передаётся в адаптер и используется при вызове методов")
    void shouldInjectExternalNotificationServiceCorrectly() {
        // Проверяем, что адаптеры используют переданный сервис
        factory = new NotificationAdapterFactory(externalNotificationService, "email");
        NotificationAdapter adapter = factory.getNotificationAdapter();

        // Вызываем метод адаптера (например, sendPaymentNotification) и проверяем, что сервис используется
        User user = new User(); // Предполагаем, что есть класс User
        adapter.sendPaymentNotification(user, "Test message");

        // Проверяем, что метод sendNotification был вызван
        verify(externalNotificationService).sendNotification(user, "Test message", "Уведомление об оплате (Email)");
    }
}