package gigabank.accountmanagement.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapterFactory {
    private final ExternalNotificationService externalNotificationService;
    private final String notificationType;

    public NotificationAdapterFactory(ExternalNotificationService externalNotificationService,
                                      @Value("${notification.type:email}") String notificationType) {
        this.externalNotificationService = externalNotificationService;
        this.notificationType = notificationType;
    }

    public NotificationAdapter getNotificationAdapter() {
        return switch (notificationType.toLowerCase()) {
            case "email" -> new EmailNotificationAdapter(externalNotificationService);
            case "sms" -> new SmsNotificationAdapter(externalNotificationService);
            case "external" -> new ExternalNotificationAdapter(externalNotificationService);
            default -> throw new IllegalArgumentException("Неизвестный тип уведомления: " + notificationType);
        };
    }
}
