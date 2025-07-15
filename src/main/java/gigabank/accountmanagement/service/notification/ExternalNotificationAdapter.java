package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;
import org.springframework.stereotype.Service;

/**
 * Адаптер для интеграции с ExternalNotificationService.
 * Преобразует данные User в строковые параметры и отправляет уведомления.
 */
@Service
public class ExternalNotificationAdapter implements NotificationAdapter {
    private final ExternalNotificationService externalNotificationService;

    public ExternalNotificationAdapter(ExternalNotificationService externalNotificationService) {
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void sendPaymentNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление об оплате");
    }

    @Override
    public void sendRefundNotification(User user, String message) {
        externalNotificationService.sendNotification(user, message, "Уведомление о возврате средств");
    }
}