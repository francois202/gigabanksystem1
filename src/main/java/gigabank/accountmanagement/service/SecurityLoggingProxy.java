package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.payment.strategies.PaymentStrategy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

public class SecurityLoggingProxy {
    private final BankAccountService bankAccountService;
    private final Random random;

    public SecurityLoggingProxy(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
        this.random = new Random();
    }

    private boolean checkAccess() {
        System.out.println("Проверка доступа для выполнения операции...");
        boolean accessGranted = random.nextBoolean();
        if (accessGranted) {
            System.out.println("Доступ разрешён. Выполняем операцию...");
        } else {
            System.out.println("Доступ запрещён. Операция отменена.");
        }
        return accessGranted;
    }

    public void processPayment(BankAccount account, BigDecimal amount, PaymentStrategy strategy, Map<String, String> details) {
        if (checkAccess()) {
            bankAccountService.processPayment(account, amount, strategy, details);
        }
    }
}
