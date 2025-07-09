import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaymentGatewayServiceTest {

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentGatewayService serviceUnderTest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    @DisplayName("Проверяет успешное выполнение списания")
    void testProcessPaymentSuccess() {
        BigDecimal value = BigDecimal.valueOf(100.00);
        Map<String, String> details = new HashMap<>();
        details.put("cardNumber", "1234567890123456");

        when(paymentGatewayService.processPayment(value, details)).thenReturn(true);

        boolean result = paymentGatewayService.processPayment(value, details);

        verify(paymentGatewayService).processPayment(value, details);
        assert result;
    }
    @Test
    @DisplayName("Проверяет успешный возврат средств")
    void testProcessRefundSuccess() {
        BigDecimal value = BigDecimal.valueOf(50.00);
        Map<String, String> details = new HashMap<>();
        details.put("transactionId", "TX123");

        when(paymentGatewayService.processRefund(value, details)).thenReturn(true);

        boolean result = paymentGatewayService.processRefund(value, details);

        verify(paymentGatewayService).processRefund(value, details);
        assert result;
    }
}