import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.paymentstrategy.BankTransferStrategy;
import gigabank.accountmanagement.service.paymentstrategy.CardPaymentStrategy;
import gigabank.accountmanagement.service.paymentstrategy.DigitalWalletPaymentStrategy;
import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PaymentStrategyTest {
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private BankAccount bankAccount;
    private BigDecimal initialBalance;
    private BigDecimal paymentAmount;
    private Map<String, String> details;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        initialBalance = new BigDecimal("1000.00");
        paymentAmount = new BigDecimal("200.00");
        bankAccount = new BankAccount("1", new ArrayList<>());
        bankAccount.setBalance(initialBalance);
        details = new HashMap<String, String>();
        when(paymentGatewayService.processPayment(paymentAmount, details)).thenReturn(true);
    }

    @Test
    @DisplayName("Проверяет уменьшение баланса при оплате картой")
    void testCardPaymentDecreasesBalance() {
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "Тестовый Продавец");
        CardPaymentStrategy strategy = new CardPaymentStrategy();

        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }
        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Card Payment", bankAccount.getTransactions().get(0).getCategory());
    }

    @Test
    @DisplayName("Проверяет уменьшение баланса при банковском переводе")
    void testBankTransferDecreasesBalance() {
        details.put("bankName", "Тестовый Банк");
        BankTransferStrategy strategy = new BankTransferStrategy();

        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }
        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Bank Transfer", bankAccount.getTransactions().get(0).getCategory());
    }

    @Test
    @DisplayName("Проверяет уменьшение баланса при оплате через цифровой кошелёк")
    void testDigitalWalletPaymentDecreasesBalance() {
        details.put("digitalWalletId", "wallet123");
        DigitalWalletPaymentStrategy strategy = new DigitalWalletPaymentStrategy();

        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }

        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Wallet Payment", bankAccount.getTransactions().get(0).getCategory());
    }
}