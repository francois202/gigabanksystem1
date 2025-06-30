package gigabank.accountmanagement.service;

import java.math.BigDecimal;
import java.util.Map;

public class PaymentGatewayService {
    private static volatile PaymentGatewayService instance;

    PaymentGatewayService() {
        try {
            System.out.println("Установление соединения с внешним платежным шлюзом...");
            Thread.sleep(2000); // Имитация задержки
            System.out.println("Соединение установлено");
        } catch (InterruptedException e) {
            System.out.println("Не удалось установить соединение");
            Thread.currentThread().interrupt();
        }
    }
    public static PaymentGatewayService getInstance() {
        if (instance == null) {
            synchronized (PaymentGatewayService.class) {
                if (instance == null) {
                    instance = new PaymentGatewayService();
                }
            }
        }
        return instance;
    }
    /**
     * Выполняет списание средств через внешний платежный шлюз.
     * @param value Сумма для списания
     * @param details Детали транзакции (например, номер карты, имя банка)
     * @return true, если списание успешно
     */
    public boolean processPayment(BigDecimal value,Map<String,String> details) {
        System.out.println("Обработка платежа: "+ value +" с указанием реквизитов: " + details);
        // Симуляция обработки платежа
        return true;
    }
    /**
     * Выполняет возврат средств через внешний платежный шлюз.
     * @param value Сумма для возврата
     * @param details Детали транзакции
     * @return true, если возврат успешен
     */
    public boolean processRefund(BigDecimal value,Map<String,String> details) {
        System.out.println("Обработка возврата средств: "+ value +" с указанием подробной информации: " + details);
        // Симуляция возврата
        return true;
    }
}
