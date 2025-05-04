package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Refund;

/**
 * Сервис осуществляющий возвраты по платежам
 */
public class RefundService {
    private final PaymentGatewayService paymentGatewayService;

    public RefundService(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    public void createRefund(Refund refund) {
        paymentGatewayService.refund(refund.getDescription(), refund.getAmount());
    }
}
