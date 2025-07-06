package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private final Map<User, List<BankAccount>> userBankAccounts;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationAdapter notificationAdapter;

    public BankAccountService(PaymentGatewayService paymentGatewayService,NotificationAdapter notificationAdapter) {
        this.paymentGatewayService = paymentGatewayService.getInstance();
        this.notificationAdapter = notificationAdapter;
        this.userBankAccounts = new HashMap<>();
    }
    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String,String> details) {
        Objects.requireNonNull(bankAccount, "BankAccount must not be null");
        Objects.requireNonNull(strategy, "PaymentStrategy must not be null");
        Objects.requireNonNull(details, "Details map must not be null");

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Payment value must be positive and non-null");
        }
        boolean success = paymentGatewayService.processPayment(value,details);
        if (!success) {
            throw new RuntimeException("Payment could not be processed: check the transaction details -" + details);
        }
        bankAccount.setBalance(bankAccount.getBalance().subtract(value));
        strategy.process(bankAccount, value, details);
        User user = bankAccount.getOwner();
        String message = String.format("Платеж на сумму %s успешно выполнен. Категория: %s. Платеж успешно обработан для счета: %s", value, details.getOrDefault("category", "Не указана"), bankAccount.getId());
        notificationAdapter.sendPaymentNotification(user, message);
        System.out.println(message);
    }
}





