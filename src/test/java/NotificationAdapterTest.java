import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Проверка паттерна Adapter")
public class NotificationAdapterTest {
    private final static String INFO = "Info";
    private final static String INFO_IN_BODY = "Info in body";

    private User user;
    private NotificationAdapter adapter;
    private ExternalNotificationService notificationService;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("example@mail.ru");
        user.setPhoneNumber("+7 925 236 22 69");

        adapter = new NotificationAdapter(user);
        notificationService = new ExternalNotificationService();
    }

    @Test
    @DisplayName("")
    public void should_send_email_and_sms() {
        notificationService.sendEmail(adapter.getEmail(), INFO, INFO_IN_BODY);
        notificationService.sendSms(adapter.getPhone(), INFO);
    }
}
