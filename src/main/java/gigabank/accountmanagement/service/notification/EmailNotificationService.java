package gigabank.accountmanagement.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("emailNotificationService")
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String recipient, String message) {
        System.out.println("Sending email to " + recipient + ": " + message);
    }
}
