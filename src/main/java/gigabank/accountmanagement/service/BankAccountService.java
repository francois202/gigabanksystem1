package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
public class BankAccountService {
    private static final Logger logger = Logger.getLogger(BankAccountService.class.getName());

    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;

    @Value("${notification.sender.email}")
    private String senderEmail;

    @Value("${notification.sender.phone}")
    private String senderPhone;

    @Autowired
    public BankAccountService(
            PaymentGatewayService paymentGatewayService,
            @Qualifier("emailNotificationService") NotificationService notificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        logger.info("BankAccountService bean is being initialized");
        logger.info("Configured sender email: " + senderEmail);
        logger.info("Configured sender phone: " + senderPhone);
    }

    @PreDestroy
    public void cleanup() {
        logger.info("BankAccountService bean is being destroyed");
    }

    public BankAccount findAccountById(int accountId) {
        return createTestAccount();
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void withdraw(Integer accountId, BigDecimal amount, String description) {
        BankAccount account = findAccountById(accountId);
        account.setBalance(account.getBalance().subtract(amount));
        // ... создание транзакции ...
    }

    public void deposit(Integer accountId, BigDecimal amount, String description) {
        BankAccount account = findAccountById(accountId);
        account.setBalance(account.getBalance().add(amount));
        // ... создание транзакции ...
    }

    public void transfer(Integer fromAccountId, Integer toAccountId,
                         BigDecimal amount, String description) {
        withdraw(fromAccountId, amount, "Transfer to " + toAccountId + ": " + description);
        deposit(toAccountId, amount, "Transfer from " + fromAccountId + ": " + description);
    }

    public void processCardPayment(BankAccount account, BigDecimal amount, String cardNumber, String merchantName) {
        withdraw(account, amount);
        logger.info("Processing card payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        notificationService.sendNotification(
                account.getOwner().getPhoneNumber(),
                "Произошел платеж по карте на сумму " + amount
        );
    }

    public void processBankTransfer(BankAccount account, BigDecimal amount, String bankName) {
        withdraw(account, amount);
        logger.info("Processing bank transfer for account " + account.getId());
        paymentGatewayService.authorize("Банковский перевод", amount);
        notificationService.sendNotification(
                account.getOwner().getEmail(),
                "Произошел банковский перевод на сумму " + amount
        );
    }

    public void processWalletPayment(BankAccount account, BigDecimal amount, String walletId) {
        withdraw(account, amount);
        logger.info("Processing wallet payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж через кошелек", amount);
        notificationService.sendNotification(
                account.getOwner().getEmail(),
                "Произошел платеж через кошелек на сумму " + amount
        );
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

        Transaction transaction1 = new Transaction(
                "tx001",
                new BigDecimal("100.00"),
                TransactionType.PAYMENT,
                "Electronics",
                LocalDateTime.now().minusDays(5)
        );

        Transaction transaction2 = new Transaction(
                "tx002",
                new BigDecimal("200.00"),
                TransactionType.DEPOSIT,
                "Groceries",
                LocalDateTime.now().minusDays(2)
        );

        account.getTransactions().addAll(List.of(transaction1, transaction2));

        return account;
    }
}