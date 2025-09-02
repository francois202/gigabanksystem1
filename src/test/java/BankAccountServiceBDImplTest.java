import gigabank.accountmanagement.db.DBConnectionManager;
import gigabank.accountmanagement.db.DBManager;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.dbimpl.BankAccountServiceBDImpl;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static gigabank.accountmanagement.db.CreateTables.initializeDatabase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankAccountServiceBDImplTest {

    private DBManager dbManager;
    private NotificationService notificationService;
    private PaymentGatewayService paymentGatewayService;
    private BankAccountServiceBDImpl bankAccountService;

    private BankAccount testAccount;
    private BankAccount testAccount2;
    private User testUser;
    private User testUser2;

    @BeforeEach
    void setUp() {
        dbManager = mock(DBManager.class);
        notificationService = mock(NotificationService.class);
        paymentGatewayService = mock(PaymentGatewayService.class);

        initializeDatabase();

        bankAccountService = new BankAccountServiceBDImpl(notificationService, paymentGatewayService, dbManager);

        testUser = new User();
        testUser.setId("user123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");

        testUser2 = new User();
        testUser2.setId("user456");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane.smith@example.com");

        testAccount = new BankAccount();
        testAccount.setId("acc123");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setOwner(testUser);

        testAccount2 = new BankAccount();
        testAccount2.setId("acc456");
        testAccount2.setBalance(new BigDecimal("500.00"));
        testAccount2.setOwner(testUser2);
    }

    @Test
    void testDeposit_Success() {
        BigDecimal depositAmount = new BigDecimal("500.00");
        BigDecimal initialBalance = testAccount.getBalance();
        BigDecimal expectedBalance = initialBalance.add(depositAmount);

        bankAccountService.deposit(testAccount, depositAmount);

        assertEquals(expectedBalance, testAccount.getBalance());
    }
}