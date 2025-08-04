package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;

public interface NotificationService {
    void sendPaymentNotification(User user);
}
