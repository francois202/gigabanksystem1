package gigabank.accountmanagement.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Внешняя платежная система
@Service
public class PaymentGatewayService {
    public PaymentGatewayService() {
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
