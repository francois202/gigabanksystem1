package gigabank.accountmanagement.service.notification;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void sendNotification(String consumer, String description) {
        System.out.println("Mail send to " + consumer + ": " + description);
    }
}
