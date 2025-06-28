import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.BankTransferStrategy;
import gigabank.accountmanagement.service.CardPaymentStrategy;
import gigabank.accountmanagement.service.DigitalWalletPaymentStrategy;
import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentStrategyTest {

    private BankAccount bankAccount;
    private BigDecimal initialBalance;
    private BigDecimal paymentAmount;
    private Map<String, String> details;
    private PaymentGatewayService paymentGatewayService;

    @BeforeEach
    void setUp() {
        initialBalance = new BigDecimal("1000.00");
        paymentAmount = new BigDecimal("200.00");
        bankAccount = new BankAccount("1", new ArrayList<>());
        bankAccount.setBalance(initialBalance);
        details = new HashMap<>();
        paymentGatewayService = PaymentGatewayService.getInstance();
    }

    @Test
    void testCardPaymentDecreasesBalance() {
        // Подготовка
        details.put("cardNumber", "1234-5678-9012-3456");
        details.put("merchantName", "Тестовый Продавец");
        CardPaymentStrategy strategy = new CardPaymentStrategy();

        // Действие
        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }

        // Проверка
        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Card Payment", bankAccount.getTransactions().get(0).getCategory());
    }

    @Test
    void testBankTransferDecreasesBalance() {
        // Подготовка
        details.put("bankName", "Тестовый Банк");
        BankTransferStrategy strategy = new BankTransferStrategy();

        // Действие
        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }

        // Проверка
        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Bank Transfer", bankAccount.getTransactions().get(0).getCategory());
    }

    @Test
    void testDigitalWalletPaymentDecreasesBalance() {
        // Подготовка
        details.put("digitalWalletId", "wallet123");
        DigitalWalletPaymentStrategy strategy = new DigitalWalletPaymentStrategy();

        // Действие
        if (paymentGatewayService.processPayment(paymentAmount, details)) {
            strategy.process(bankAccount, paymentAmount, details);
            bankAccount.setBalance(bankAccount.getBalance().subtract(paymentAmount));
        }

        // Проверка
        assertEquals(initialBalance.subtract(paymentAmount), bankAccount.getBalance());
        assertEquals(1, bankAccount.getTransactions().size());
        assertEquals("Wallet Payment", bankAccount.getTransactions().get(0).getCategory());
    }
}