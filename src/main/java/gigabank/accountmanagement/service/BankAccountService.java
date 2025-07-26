package gigabank.accountmanagement.service;

import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.securityLogging.SecurityLogging;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
public class BankAccountService implements SecurityLogging {
    private final PaymentGatewayService paymentGatewayService;
    private final ExternalNotificationService externalNotificationService;
    private final NotificationService notificationService;
    private static final Logger logger = Logger.getLogger(BankAccountService.class.getName());

    @Autowired
    public BankAccountService(PaymentGatewayService paymentGatewayService, ExternalNotificationService externalNotificationService, @Qualifier("smsNotificationService") NotificationService notificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.externalNotificationService = externalNotificationService;
        this.notificationService = notificationService;
    }

    @Value("${notification.email}")
    private String sendEmail;

    @Value("${notification.phone}")
    private String sendPhone;

    public BankAccount findAccountById(int accountId) {
        return createTestAccount();
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void withdraw(Integer accountId, BigDecimal amount, String description) {
        BankAccount account = findAccountById(accountId);
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void deposit(int accountId, BigDecimal amount, String description) {
        BankAccount account = findAccountById(accountId);
        account.setBalance(account.getBalance().add(amount));
    }

    public void transfer(int fromAccountId, int toAccountId,
                         BigDecimal amount, String description) {
        withdraw(fromAccountId, amount, "Transfer to " + toAccountId + ": " + description);
        deposit(toAccountId, amount, "Transfer from " + fromAccountId + ": " + description);
    }

    @Override
    public void securityLogging(BankAccount account, BigDecimal amount, PaymentStrategy strategy, Map<String, String> details) {
        withdraw(account, amount);
        strategy.process(account, amount, details);
    }

    public void processCardPayment(BankAccount account, BigDecimal amount, String cardNumber, String merchantName) {
        withdraw(account, amount);
        NotificationAdapter adapter = new NotificationAdapter(account.getOwner());
        //создание транзакции
        System.out.println("Processed card payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(adapter.getPhone(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(adapter.getEmail(), "Информация о платеже", "Произошел платеж по карте");
        notificationService.sendNotification(account.getOwner().getEmail(), "Произошел платеж по карте");
    }

    public void processBankTransfer(BankAccount account, BigDecimal amount, String bankName) {
        withdraw(account, amount);
        NotificationAdapter adapter = new NotificationAdapter(account.getOwner());
        //создание транзакции
        System.out.println("Processed bank transfer for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(adapter.getPhone(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(adapter.getEmail(), "Информация о платеже", "Произошел платеж по карте");
    }

    public void processWalletPayment(BankAccount account, BigDecimal amount, String walletId) {
        withdraw(account, amount);
        NotificationAdapter adapter = new NotificationAdapter(account.getOwner());
        //создание транзакции
        System.out.println("Processed wallet payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(adapter.getPhone(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(adapter.getEmail(), "Информация о платеже", "Произошел платеж по карте");
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

        Transaction transaction1 =  Transaction.builder()
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

    public void processPayment(BankAccount account, BigDecimal amount, PaymentStrategy strategy, Map<String, String> details) {
        withdraw(account, amount);
        strategy.process(account, amount, details);
    }

    @PostConstruct
    public void initMethod() {
        logger.info("Инициализация перед созданием бина bankAccountService");
        logger.info("Send email: " + sendEmail);
        logger.info("Send phone: " + sendPhone);
    }

    @PreDestroy
    public void destroyMethod() {
        logger.info("Инициализация после уничтожения бина bankAccountService");
    }
}
