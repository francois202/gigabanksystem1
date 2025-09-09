package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.UserEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("smsNotificationService")
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendPaymentNotification(UserEntity userEntity) {
        System.out.println("Sms notification sent to " + userEntity.getPhoneNumber());
    }
}
