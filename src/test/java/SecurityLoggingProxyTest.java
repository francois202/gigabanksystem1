import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.*;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class SecurityLoggingProxyTest {

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private SecurityLoggingProxy securityLoggingProxy;

    private BankAccount bankAccount;
    private BigDecimal paymentAmount;
    private PaymentStrategy paymentStrategy;
    private Map<String, String> details;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        bankAccount = new BankAccount("1", new ArrayList<>());
        bankAccount.setBalance(new BigDecimal("1000.00"));
        paymentAmount = new BigDecimal("100.00");
        paymentStrategy = new CardPaymentStrategy();
        details = new HashMap<String, String>();
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "Test Merchant");
        doNothing().when(bankAccountService).processPayment(any(BankAccount.class), any(BigDecimal.class), any(PaymentStrategy.class), any(Map.class));
    }

    @Test
    @DisplayName("Проверяет успешную обработку платежа при разрешённом доступе")
    void testProcessPaymentSucceedsWhenAccessGranted() throws SQLException {
        securityLoggingProxy.setTestAccessGranted(true);
        securityLoggingProxy.processPayment(bankAccount, paymentAmount, paymentStrategy, details);
        verify(bankAccountService, atLeastOnce()).processPayment(bankAccount, paymentAmount, paymentStrategy, details);
    }

    @Test
    @DisplayName("Проверяет отсутствие обработки платежа при запрещённом доступе")
    void testProcessPaymentDoesNotExecuteWhenAccessDenied() throws SQLException {
        securityLoggingProxy.setTestAccessGranted(false);
        securityLoggingProxy.processPayment(bankAccount, paymentAmount, paymentStrategy, details);
        verify(bankAccountService, never()).processPayment(any(BankAccount.class), any(BigDecimal.class), any(PaymentStrategy.class), any(Map.class));
    }
}