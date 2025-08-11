package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import gigabank.accountmanagement.service.payment.strategies.PaymentStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;

    public BankAccountService(NotificationService notificationService) {
        this.paymentGatewayService = PaymentGatewayService.getPaymentGatewayService();
        this.notificationService = notificationService;
    }

    public BankAccount findAccountById(int accountId) {
        return createTestAccount();
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void processPayment(BankAccount account, BigDecimal amount,
                               PaymentStrategy strategy, Map<String, String> details) {
        strategy.process(account, amount, details);
    }

    public static BankAccount createTestAccount() {
        User testUser = new User();
        testUser.setId("user123");
        testUser.setFirstName("John");
        testUser.setMiddleName("K");
        testUser.setLastName("Doe");
        testUser.setBirthDate(LocalDateTime.now().minusYears(25).toLocalDate());
        testUser.setEmail("john.doe@example.com");
        testUser.setPhoneNumber("+1234567890");

        BankAccount account = new BankAccount();
        account.setId("acc123");
        account.setBalance(new BigDecimal("5000.00"));
        account.setOwner(testUser);

        Transaction transaction1 = Transaction.builder()
                .id("tx001")
                .value(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .category("Electronics")
                .createdDate(LocalDateTime.now().minusDays(5))
                .build();

        Transaction transaction2 = Transaction.builder()
                .id("tx002")
                .value(new BigDecimal("200.00"))
                .type(TransactionType.DEPOSIT)
                .category("Groceries")
                .createdDate(LocalDateTime.now().minusDays(2))
                .build();

        account.getTransactions().addAll(List.of(transaction1, transaction2));

        return account;
    }
}
