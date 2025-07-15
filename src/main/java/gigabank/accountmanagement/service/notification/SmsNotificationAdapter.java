package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;
import org.springframework.stereotype.Service;

@Service
public class SmsNotificationAdapter implements NotificationAdapter {
    private final ExternalNotificationService externalNotificationService;

    public SmsNotificationAdapter(ExternalNotificationService externalNotificationService) {
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void sendPaymentNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление об оплате (SMS)");
    }

    @Override
    public void sendRefundNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление о возврате средств (SMS)");
    }
}