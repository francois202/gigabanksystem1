package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationAdapter implements NotificationAdapter {
    private final ExternalNotificationService externalNotificationService;

    public EmailNotificationAdapter(ExternalNotificationService externalNotificationService) {
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void sendPaymentNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление об оплате (Email)");
    }

    @Override
    public void sendRefundNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление о возврате средств (Email)");
    }
}