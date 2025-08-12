package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import gigabank.accountmanagement.service.payment.strategies.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
public class BankAccountService {
    private static final Logger logger = Logger.getLogger(BankAccountService.class.getName());
    private final PaymentGatewayService paymentGatewayService;
    @Qualifier("emailNotificationService")
    private final NotificationService notificationService;

    @Value("${notification.sender.email}")
    private String senderEmail;

    @Value("${notification.sender.phone}")
    private String senderPhone;

    @Autowired
    public BankAccountService(NotificationService notificationService,
                              PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
        logger.info("Initializing test accounts data...");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct method called - BankAccountService bean initialization complete");
        logger.info("Configured sender email: " + senderEmail);
        logger.info("Configured sender phone: " + senderPhone);
    }

    @PreDestroy
    public void cleanup() {
        logger.info("PreDestroy method called - BankAccountService bean is about to be destroyed");
    }

    public BankAccount findAccountById(int accountId) {
        return createTestAccount();
    }

    public void deposit(BankAccount account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
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
