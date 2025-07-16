package gigabank.accountmanagement.service.notification;

import gigabank.accountmanagement.entity.User;

public class NotificationAdapter implements NotificationAdapterService{
    private final User user;

    public NotificationAdapter(User user) {
        this.user = user;
    }

    @Override
    public String getPhone() {
        return user.getPhoneNumber();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }
}
