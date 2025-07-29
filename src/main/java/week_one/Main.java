package week_one;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Создаем пользователя
        User user = new User("user1", "John Doe");

        // Создаем сервис
        BankService bankService = new BankService();

        // Создаем счета
        bankService.createAccount(user, "ACC123");
        bankService.createAccount(user, "ACC456");

        // Получаем счета пользователя
        List<BankAccount> accounts = user.getAccounts();
        BankAccount acc1 = accounts.get(0);
        BankAccount acc2 = accounts.get(1);

        // Пополняем первый счет
        acc1.deposit(new BigDecimal("1000"));

        // Переводим средства между счетами
        bankService.transfer(acc1, acc2, new BigDecimal("500"));

        // Логируем балансы
        log.info("Balance of {}: {}", acc1.getAccountNumber(), acc1.getBalance());
        log.info("Balance of {}: {}", acc2.getAccountNumber(), acc2.getBalance());

        // Логируем историю транзакций
        log.info("Transaction history for {}:", acc1.getAccountNumber());
        bankService.getTransactionHistory(acc1).forEach(tx -> log.info("{}", tx));
    }
}