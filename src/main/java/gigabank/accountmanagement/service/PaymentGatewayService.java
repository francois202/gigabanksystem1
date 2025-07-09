package gigabank.accountmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class PaymentGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);

    public PaymentGatewayService() {
        try {
            System.out.println("Установление соединения с внешним платежным шлюзом...");
            Thread.sleep(2000); // Имитация задержки
            System.out.println("Соединение установлено");
        } catch (InterruptedException e) {
            System.out.println("Не удалось установить соединение");
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Выполняет списание средств через внешний платежный шлюз.
     * @param value Сумма для списания
     * @param details Детали транзакции (например, номер карты, имя банка)
     * @return true, если списание успешно
     */
    public boolean processPayment(BigDecimal value,Map<String,String> details) {
        System.out.println("Обработка платежа: "+ value +" с указанием реквизитов: " + details);
        logger.info("Обработка платежа: {} с указанием реквизитов: {}", value, details);
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
        logger.info("Обработка возврата средств: {} с указанием подробной информации: {}", value, details);
        return true;
    }
    @PostConstruct
    public void init() {
        logger.info("Инициализация PaymentGatewayService. Установка соединения завершена.");
    }
    @PreDestroy
    public void destroy() {
        logger.info("Завершение работы PaymentGatewayService. Освобождение ресурсов.");
    }
}