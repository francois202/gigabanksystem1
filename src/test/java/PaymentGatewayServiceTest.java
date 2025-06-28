import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class PaymentGatewayServiceTest {
    @Test
    void testGetInstanceReturnsSameObject() {
        // Act
        PaymentGatewayService instance1 = PaymentGatewayService.getInstance();
        PaymentGatewayService instance2 = PaymentGatewayService.getInstance();

        // Assert
        assertSame(instance1, instance2, "getInstance() должен возвращать тот же объект");
    }
}
