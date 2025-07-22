package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

/**
 * Прокси для BankAccountService, добавляющий проверку доступа перед выполнением операций.
 */
@Component
public class SecurityLoggingProxy {
    private final BankAccountService bankAccountService;
    @Setter
    private boolean testAccessGranted = false;

    public SecurityLoggingProxy(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String, String> details) throws SQLException {
        System.out.println("Проверка доступа для выполнения операции...");
        if (testAccessGranted) {
            System.out.println("Доступ разрешён. Выполняем операцию...");
            bankAccountService.processPayment(bankAccount, value, strategy, details);
        } else {
            System.out.println("Доступ запрещён. Операция отменена.");
        }
    }
}