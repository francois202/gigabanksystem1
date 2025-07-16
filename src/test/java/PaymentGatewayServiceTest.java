import gigabank.accountmanagement.service.PaymentGatewayService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Проверка паттерна Singleton")
public class PaymentGatewayServiceTest {

    @Test
    @DisplayName("Проверка что Singleton возвращает один и тот же экземпляр")
    public void payment_gateway_service_should_be_return_identical_objects() {
        PaymentGatewayService service1 = PaymentGatewayService.getInstance();
        PaymentGatewayService service2 = PaymentGatewayService.getInstance();

        Assertions.assertSame(service1, service2);
        Assertions.assertEquals(service1, service2);
    }
}
