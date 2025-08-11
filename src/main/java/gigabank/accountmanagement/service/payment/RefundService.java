package gigabank.accountmanagement.service.payment;

import gigabank.accountmanagement.entity.Refund;

/**
 * Сервис осуществляющий возвраты по платежам
 */
public class RefundService {
    private final PaymentGatewayService paymentGatewayService;

    public RefundService() {
        this.paymentGatewayService = PaymentGatewayService.getPaymentGatewayService();
    }

    public void createRefund(Refund refund) {
        paymentGatewayService.refund(refund.getDescription(), refund.getAmount());
    }
}
