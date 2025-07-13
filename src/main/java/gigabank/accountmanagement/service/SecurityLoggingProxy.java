package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Прокси для BankAccountService, добавляющий проверку доступа перед выполнением операций.
 */
@Component
public class SecurityLoggingProxy {
    private final BankAccountService bankAccountService;
    // Метод для тестов
    @Setter
    private boolean testAccessGranted = false;

    @Autowired
    public SecurityLoggingProxy(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String, String> details) {
        System.out.println("Проверка доступа для выполнения операции...");
        if (testAccessGranted) {
            System.out.println("Доступ разрешён. Выполняем операцию...");
            bankAccountService.processPayment(bankAccount, value, strategy, details);
        } else {
            System.out.println("Доступ запрещён. Операция отменена.");
        }
    }
}