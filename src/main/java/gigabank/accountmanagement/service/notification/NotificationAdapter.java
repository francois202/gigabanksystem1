package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;

/**
 * Интерфейс адаптера для отправки уведомлений пользователю.
 */
public interface NotificationAdapter {
    void sendPaymentNotification(User user, String message);

    void sendRefundNotification(User user, String message);
}