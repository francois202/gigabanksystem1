package gigabank.accountmanagement;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.service.BankAccountService;
import gigabank.accountmanagement.paymentstrategy.PaymentStrategy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

public class SecurityLoggingProxy implements SecurityLogging {
    private final BankAccountService bankAccountService;

    public SecurityLoggingProxy(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void securityLogging(BankAccount account, BigDecimal amount, PaymentStrategy strategy, Map<String, String> details) {
        System.out.println("Проверка доступа для выполнения операции...");
        Random random = new Random();
        boolean access = random.nextBoolean();
        if (access) {
            System.out.println("Доступ разрешен. Выполняем операцию...");
            bankAccountService.securityLogging(account, amount, strategy, details);
        } else {
            System.out.println("Доступ запрещен. Отмена операции...");
        }
    }
}
