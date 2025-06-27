import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class PaymentGatewayServiceTest {
    @Test
    @DisplayName("Проверяет, что getInstance возвращает один и тот же объект")
    void testGetInstanceReturnsSameObject() {
        PaymentGatewayService instance1 = PaymentGatewayService.getInstance();
        PaymentGatewayService instance2 = PaymentGatewayService.getInstance();

        assertSame(instance1, instance2, "getInstance() должен возвращать тот же объект");
    }
}
