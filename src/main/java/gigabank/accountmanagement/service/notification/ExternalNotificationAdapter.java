package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;

/**
 * Адаптер для интеграции с ExternalNotificationService.
 * Преобразует данные User в строковые параметры и отправляет уведомления.
 */
public class ExternalNotificationAdapter implements NotificationAdapter {
    private final ExternalNotificationService externalNotificationService;

    public ExternalNotificationAdapter(ExternalNotificationService externalNotificationService) {
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void sendPaymentNotification(User user, String message) {
        if (user == null || message == null) {
            return;
        }

        String phone = user.getPhoneNumber();
        String email = user.getEmail();
        String subject = "Уведомление об оплате";

        if (phone != null && !phone.isEmpty()) {
            externalNotificationService.sendSms(phone, message);
        }
        if (email != null && !email.isEmpty()) {
            externalNotificationService.sendEmail(email, subject, message);
        }
    }

    @Override
    public void sendRefundNotification(User user, String message) {
        if (user == null || message == null) {
            return;
        }

        String phone = user.getPhoneNumber();
        String email = user.getEmail();
        String subject = "Уведомление о возврате средств";

        if (phone != null && !phone.isEmpty()) {
            externalNotificationService.sendSms(phone, message);
        }
        if (email != null && !email.isEmpty()) {
            externalNotificationService.sendEmail(email, subject, message);
        }
    }
}