package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.UserEntity;

public interface NotificationService {
    void sendPaymentNotification(UserEntity userEntity);
}
