package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;

public class NotificationAdapter implements NotificationService {
    private final ExternalNotificationService externalNotificationService;

    public NotificationAdapter(ExternalNotificationService externalNotificationService) {
        this.externalNotificationService = externalNotificationService;
    }

    @Override
    public void sendPaymentNotification(User user) {
        String smsMessage = "Произошел платеж по карте";
        String emailSubject = "Информация о платеже";
        String emailBody = "Произошел платеж по карте";

        externalNotificationService.sendSms(user.getPhoneNumber(), smsMessage);
        externalNotificationService.sendEmail(user.getEmail(), emailSubject, emailBody);
    }
}
