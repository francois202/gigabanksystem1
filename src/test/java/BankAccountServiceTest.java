import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.service.PaymentGatewayService;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletStrategy;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@DisplayName("Проверка паттерна Strategy")
public class BankAccountServiceTest {
    private static final String SPORT_CATEGORY = "Sport";
    private static final LocalDateTime TEN_DAYS_AGO = LocalDateTime.now().minusDays(10);
    private static final BigDecimal THREE_HUNDRED = new BigDecimal("300.00");
    private static final BigDecimal TWO_HUNDRED = new BigDecimal("200.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    private User user;
    private PaymentGatewayService paymentGatewayService;
    private ExternalNotificationService notificationService;
    private NotificationAdapter notificationAdapter;
    private BankAccountService bankAccountService;
    private BankAccount bankAccount;
    private Map<String, String> details;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("example@mail.ru");
        user.setPhoneNumber("+7 985 185 82 49");

        paymentGatewayService = PaymentGatewayService.getInstance();
        notificationService = new ExternalNotificationService();
        notificationAdapter = new NotificationAdapter(user);
        bankAccountService = new BankAccountService(paymentGatewayService, notificationService, notificationAdapter);

        bankAccount = new BankAccount();
        bankAccount.setBalance(THREE_HUNDRED);
        bankAccount.setOwner(user);

        Transaction transaction = Transaction.builder()
                .id("1000")
                .value(TWO_HUNDRED)
                .type(TransactionType.PAYMENT)
                .category(SPORT_CATEGORY)
                .createdDate(TEN_DAYS_AGO)
                .build();

        bankAccount.getTransactions().add(transaction);

        details = new HashMap<>();
        details.put("test", "test");
    }

    @Test
    @DisplayName("Оплата картой")
    public void pay_by_card() {
        CardPaymentStrategy strategy = new CardPaymentStrategy(paymentGatewayService, notificationService);
        bankAccountService.processPayment(bankAccount, TWO_HUNDRED, strategy, details);

        Assertions.assertEquals(ONE_HUNDRED, bankAccount.getBalance());
    }

    @Test
    @DisplayName("Оплата через банк")
    public void pay_by_bank() {
        PaymentStrategy strategy = new BankTransferStrategy(paymentGatewayService, notificationService);
        bankAccountService.processPayment(bankAccount, TWO_HUNDRED, strategy, details);

        Assertions.assertEquals(ONE_HUNDRED, bankAccount.getBalance());
    }

    @Test
    @DisplayName("Оплата цифровым кошельком")
    public void pay_by_digital_wallet() {
        PaymentStrategy strategy = new DigitalWalletStrategy(paymentGatewayService, notificationService);
        bankAccountService.processPayment(bankAccount, TWO_HUNDRED, strategy, details);

        Assertions.assertEquals(ONE_HUNDRED, bankAccount.getBalance());
    }
}
