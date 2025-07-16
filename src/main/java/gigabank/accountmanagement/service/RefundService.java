package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Refund;

/**
 * Сервис осуществляющий возвраты по платежам
 */
public class RefundService {
    private PaymentGatewayService paymentGatewayService = PaymentGatewayService.getInstance();

    public void createRefund(Refund refund) {
        paymentGatewayService.refund(refund.getDescription(), refund.getAmount());
    }
}
