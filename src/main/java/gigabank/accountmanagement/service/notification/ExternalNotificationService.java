package gigabank.accountmanagement.service;

import org.springframework.stereotype.Service;

// Внешний API уведомлений (эмулирует внешний сервис)
@Service
public class ExternalNotificationService {

    public void sendSms(String phone, String msg) {
        System.out.println("Отправка SMS на " + phone + ": " + msg);
    }


    public void sendEmail(String email, String subject, String body) {
        System.out.println("Отправка Email на " + email + ": " + subject + " - " + body);
    }
}
