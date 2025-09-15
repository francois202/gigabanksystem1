package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.model.UserEntity;

public interface NotificationService {
    void sendPaymentNotification(UserEntity userEntity);
}
