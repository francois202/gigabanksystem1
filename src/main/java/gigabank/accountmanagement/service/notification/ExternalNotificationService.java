package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;


/**
 * Сервис для отправки уведомлений через SMS и email.
 */
public class ExternalNotificationService {

    public void sendSms(String phone,String msg) {
        System.out.println("Отправка SMS на " + phone + ": " + msg);
    }

    public void sendEmail(String email,String subject, String body) {
        System.out.println("Отправка Email на " + email + ": " + subject + " - " + body);
    }

    /**
     * Отправляет уведомление пользователю через доступные каналы (SMS и/или email).
     *
     * @param user    Пользователь, которому отправляется уведомление.
     * @param message Текст сообщения.
     * @param subject Тема сообщения (для email).
     */
    public void sendNotification(User user,String message,String subject) {
        if (user == null || message == null || subject == null) {
            return;
        }
        String phone = user.getPhoneNumber();
        String email = user.getEmail();

        if (phone != null && !phone.isBlank()) {
            sendSms(phone, message);
        }
        if (email != null && !email.isBlank()) {
            sendEmail(email, subject, message);
        }
    }
}
