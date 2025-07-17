package BankAcount.src;

import java.math.BigDecimal;
import java.util.List;

public class Main {
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

        // Выводим балансы
        System.out.println("Balance of ACC123: " + acc1.getBalance());
        System.out.println("Balance of ACC456: " + acc2.getBalance());

        // Выводим историю транзакций
        System.out.println("Transaction history for ACC123:");
        bankService.getTransactionHistory(acc1).forEach(System.out::println);
    }
}