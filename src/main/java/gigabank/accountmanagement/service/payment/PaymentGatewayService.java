package gigabank.accountmanagement.service.payment;

import java.math.BigDecimal;

// Внешняя платежная система
public class PaymentGatewayService {

    private static PaymentGatewayService paymentGatewayService;

    public static PaymentGatewayService getPaymentGatewayService() {
        if (paymentGatewayService == null) {
            paymentGatewayService = new PaymentGatewayService();
        }
        return paymentGatewayService;
    }

    private PaymentGatewayService() {
        System.out.println("Создано новое подключение к платёжной системе...");
    }

    public boolean authorize(String txId, BigDecimal amount) {
        //здесь эмуляция вызова внешней платежной системы
        System.out.println("Авторизация транзакции " + txId + " на сумму " + amount);
        return true;
    }

    public boolean refund(String txId, BigDecimal amount) {
        //здесь эмуляция вызова внешней платежной системы
        System.out.println("Осуществление возврата " + txId+ " на сумму " + amount);
        return true;
    }
}
