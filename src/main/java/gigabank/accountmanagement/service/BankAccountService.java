package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.ExternalNotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private final PaymentGatewayService paymentGatewayService;
    private final ExternalNotificationService externalNotificationService;

    public BankAccountService(PaymentGatewayService paymentGatewayService, ExternalNotificationService externalNotificationService) {
        this.paymentGatewayService = paymentGatewayService;
        this.externalNotificationService = externalNotificationService;
    }

    public BankAccount findAccountById(int accountId) {
        return createTestAccount();
    }

    public void withdraw(BankAccount account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
    }

    public void processCardPayment(BankAccount account, BigDecimal amount, String cardNumber, String merchantName) {
        withdraw(account, amount);
        //создание транзакции
        System.out.println("Processed card payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(account.getOwner().getPhoneNumber(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(account.getOwner().getEmail(), "Информация о платеже", "Произошел платеж по карте");
    }

    public void processBankTransfer(BankAccount account, BigDecimal amount, String bankName) {
        withdraw(account, amount);
        //создание транзакции
        System.out.println("Processed bank transfer for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(account.getOwner().getPhoneNumber(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(account.getOwner().getEmail(), "Информация о платеже", "Произошел платеж по карте");
    }

    public void processWalletPayment(BankAccount account, BigDecimal amount, String walletId) {
        withdraw(account, amount);
        //создание транзакции
        System.out.println("Processed wallet payment for account " + account.getId());
        paymentGatewayService.authorize("Платеж по карте", amount);
        externalNotificationService.sendSms(account.getOwner().getPhoneNumber(), "Произошел платеж по карте");
        externalNotificationService.sendEmail(account.getOwner().getEmail(), "Информация о платеже", "Произошел платеж по карте");
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
