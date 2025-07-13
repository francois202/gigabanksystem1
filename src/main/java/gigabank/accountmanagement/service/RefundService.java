package gigabank.accountmanagement.service;

import gigabank.accountmanagement.annotation.LogExecutionTime;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для обработки возвратов денежных средств.
 */
@Service
public class RefundService {
    private final NotificationAdapter notificationAdapter;
    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public RefundService(PaymentGatewayService paymentGatewayService, @Qualifier("email") NotificationAdapter notificationAdapter) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationAdapter = notificationAdapter;
    }

    public void processRefund(BankAccount bankAccount, BigDecimal value, Map<String, String> details) {
        boolean success = paymentGatewayService.processRefund(value, details);
        if (success) {
            String id = UUID.randomUUID().toString();
            Transaction transaction = Transaction.builder()
                    .id(id)
                    .value(value.negate()) // Отрицательная сумма для возврата
                    .type(TransactionType.REFUND)
                    .category("Refund")
                    .bankAccount(bankAccount)
                    .createdDate(LocalDateTime.now())
                    .merchantName(details.get("merchantName"))
                    .cardNumber(details.get("cardNumber"))
                    .build();
            bankAccount.getTransactions().add(transaction);

            // Отправляем уведомление
            User user = bankAccount.getOwner();
            String message = String.format("Возврат средств на сумму %s успешно выполнен. Merchant: %s", value, details.getOrDefault("merchantName", "Не указан"));
            notificationAdapter.sendRefundNotification(user, message);

            System.out.println("Возврат средств: " + value + " успешно обработан для учетной записи: " + bankAccount.getId());
        } else {
            System.out.println("Не удалось осуществить возврат средств для учетной записи: " + bankAccount.getId());
            throw new RuntimeException("Не удалось выполнить обработку возврата средств.");
        }
    }
}