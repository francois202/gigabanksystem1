package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.entity.UserEntity;
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

    public BankAccountEntity findAccountById(int accountId) {
        return createTestAccount();
    }

    public void deposit(BankAccountEntity account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
    }

    public void withdraw(BankAccountEntity account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void processPayment(BankAccountEntity account, BigDecimal amount,
                               PaymentStrategy strategy, Map<String, String> details) {
        strategy.process(account, amount, details);
    }

    public static BankAccountEntity createTestAccount() {
        UserEntity testUserEntity = new UserEntity();
        testUserEntity.setId(123L);
        testUserEntity.setName("John");
        testUserEntity.setEmail("john.doe@example.com");
        testUserEntity.setPhoneNumber("+1234567890");

        BankAccountEntity account = new BankAccountEntity();
        account.setId(124L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setOwner(testUserEntity);

        TransactionEntity transactionEntity1 = TransactionEntity.builder()
                .id(125L)
                .value(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .category("Electronics")
                .createdDate(LocalDateTime.now().minusDays(5))
                .build();

        TransactionEntity transactionEntity2 = TransactionEntity.builder()
                .id(126L)
                .value(new BigDecimal("200.00"))
                .type(TransactionType.DEPOSIT)
                .category("Groceries")
                .createdDate(LocalDateTime.now().minusDays(2))
                .build();

        account.getTransactionEntities().addAll(List.of(transactionEntity1, transactionEntity2));

        return account;
    }
}
