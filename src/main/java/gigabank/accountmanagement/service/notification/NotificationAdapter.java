package gigabank.accountmanagement.notification;

import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.NotificationAdapterService;
import org.springframework.stereotype.Component;

@Component
public class NotificationAdapter implements NotificationAdapterService {
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
