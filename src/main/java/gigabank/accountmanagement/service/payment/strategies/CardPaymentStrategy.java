package gigabank.accountmanagement.service.payment.strategies;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CardPaymentStrategy implements PaymentStrategy {
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;

    @Autowired
    public CardPaymentStrategy(PaymentGatewayService paymentGatewayService,
                               @Qualifier("emailNotificationService") NotificationService notificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
    }

    @Override
    public void process(BankAccount account, BigDecimal amount, Map<String, String> details) {
        account.setBalance(account.getBalance().subtract(amount));

        String cardNumber = details.get("cardNumber");
        String merchantName = details.get("merchantName");

        Transaction.builder()
                .id(account.getId())
                .value(amount)
                .type(TransactionType.PAYMENT)
                .createdDate(LocalDateTime.now())
                .cardNumber(cardNumber)
                .merchantName(merchantName)
                .build();

        System.out.println("Processed card payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);

        notificationService.sendPaymentNotification(account.getOwner());
    }
}
