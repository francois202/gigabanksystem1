package gigabank.accountmanagement.service.payment.strategies;

import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BankTransferStrategy implements PaymentStrategy {
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;

    @Autowired
    public BankTransferStrategy(PaymentGatewayService paymentGatewayService,
                                @Qualifier("emailNotificationService") NotificationService notificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
    }

    @Override
    public void process(BankAccountEntity account, BigDecimal amount, Map<String, String> details) {
        account.setBalance(account.getBalance().subtract(amount));

        String bankName = details.get("bankName");

        TransactionEntity.builder()
                .id(account.getId())
                .value(amount)
                .type(TransactionType.PAYMENT)
                .createdDate(LocalDateTime.now())
                .build();

        System.out.println("Processed bank transfer for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);

        notificationService.sendPaymentNotification(account.getOwner());
    }
}
