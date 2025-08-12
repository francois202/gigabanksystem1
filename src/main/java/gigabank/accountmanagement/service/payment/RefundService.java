package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.Refund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис осуществляющий возвраты по платежам
 */
@Service
public class RefundService {
    private final PaymentGatewayService paymentGatewayService;

    @Autowired
    public RefundService(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    public void createRefund(Refund refund) {
        paymentGatewayService.refund(refund.getDescription(), refund.getAmount());
    }
}
