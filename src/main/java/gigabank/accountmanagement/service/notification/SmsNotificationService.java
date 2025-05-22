package gigabank.accountmanagement.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("smsNotificationService")
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String phone, String message) {
        System.out.println("Sending SMS to " + phone + ": " + message);
    }
}