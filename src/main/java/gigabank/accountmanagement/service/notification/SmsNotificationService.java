package gigabank.accountmanagement.service.notification;

import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String consumer, String description) {
        System.out.println("Message send to " + consumer + ": " + description);
    }
}
