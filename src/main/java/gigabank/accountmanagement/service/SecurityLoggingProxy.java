package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

/**
 * Прокси для BankAccountService, добавляющий проверку доступа перед выполнением операций.
 */
public class SecurityLoggingProxy {

    private final BankAccountService bankAccountService;
    private final Random random = new Random();

    public SecurityLoggingProxy(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String, String> details) {
        System.out.println("Проверка доступа для выполнения операции...");
        boolean accessGranted = random.nextBoolean();

        if (accessGranted) {
            System.out.println("Доступ разрешён. Выполняем операцию...");
            bankAccountService.processPayment(bankAccount, value, strategy, details);
        } else {
            System.out.println("Доступ запрещён. Операция отменена.");
        }
    }
}