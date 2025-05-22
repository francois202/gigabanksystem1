package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Refund;
import org.springframework.stereotype.Service;

/**
 * Сервис осуществляющий возвраты по платежам
 */
@Service
public class RefundService {
    private final PaymentGatewayService paymentGatewayService;

    public RefundService(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    public void createRefund(Refund refund) {
        paymentGatewayService.refund(refund.getDescription(), refund.getAmount());
    }
}
