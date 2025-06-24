package gigabank.accountmanagement.service;

import gigabank.accountmanagement.annotation.LogExecutionTime;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private Map<User, List<BankAccount>> userBankAccounts;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationAdapter notificationAdapter;

    public BankAccountService(PaymentGatewayService paymentGatewayService, NotificationAdapter notificationAdapter) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationAdapter = notificationAdapter;
        this.userBankAccounts = new HashMap<>();
    }


    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String, String> details) {
        if (bankAccount == null || value == null || value.compareTo(BigDecimal.ZERO) <= 0 || strategy == null) {
            throw new IllegalArgumentException("Некорректные параметры платежа");
        }

        // Обрабатываем платеж через PaymentGatewayService
        boolean success = paymentGatewayService.processPayment(value, details);
        if (success) {
            // Уменьшаем баланс счета
            bankAccount.setBalance(bankAccount.getBalance().subtract(value));

            // Создаем транзакцию с помощью стратегии
            strategy.process(bankAccount, value, details);

            // Отправляем уведомление пользователю
            User user = bankAccount.getOwner();
            String message = String.format("Платеж на сумму %s успешно выполнен. Категория: %s", value, details.getOrDefault("category", "Не указана"));
            notificationAdapter.sendPaymentNotification(user, message);

            System.out.println("Платеж на сумму " + value + " успешно обработан для счета: " + bankAccount.getId());
        } else {
            throw new RuntimeException("Не удалось обработать платеж");
        }
    }
}




