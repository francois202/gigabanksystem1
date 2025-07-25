package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для обработки возвратов денежных средств.
 */
@Service
public class RefundService {
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationAdapter notificationAdapter;
    private final DBManager dbManager;

    public RefundService(PaymentGatewayService paymentGatewayService,
                         @Qualifier("emailNotification") NotificationAdapter notificationAdapter,
                         DBManager dbManager) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationAdapter = notificationAdapter;
        this.dbManager = dbManager;
    }

    public void processRefund(BankAccount bankAccount, BigDecimal value, Map<String, String> details) throws SQLException {
        boolean success = paymentGatewayService.processRefund(value, details);
        if (success) {
            String id = UUID.randomUUID().toString();
            Transaction transaction = Transaction.builder()
                    .id(id)
                    .value(value.negate())
                    .type(TransactionType.REFUND)
                    .category("Refund")
                    .bankAccount(bankAccount)
                    .createdDate(LocalDateTime.now())
                    .merchantName(details.get("merchantName"))
                    .cardNumber(details.get("cardNumber"))
                    .build();
            bankAccount.getTransactions().add(transaction);
            dbManager.addTransaction(id, bankAccount.getOwner().getId(), value, "REFUND",
                    Timestamp.valueOf(LocalDateTime.now()), null, null);

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