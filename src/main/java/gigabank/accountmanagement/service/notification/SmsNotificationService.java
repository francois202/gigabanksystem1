package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("smsNotificationService")
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendPaymentNotification(User user) {
        System.out.println("Sms notification sent to " + user.getPhoneNumber());
    }
}
