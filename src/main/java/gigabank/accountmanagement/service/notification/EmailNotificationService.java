package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.model.UserEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("emailNotificationService")
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendPaymentNotification(UserEntity userEntity) {
        System.out.println("Email notification sent to " + userEntity.getEmail());
    }
}
