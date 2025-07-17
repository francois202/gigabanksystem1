package BankAcount.src;

import org.example.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public class BankService {
    public BankAccount createAccount(User user, String accountNumber) {
        BankAccount account = new BankAccount(accountNumber, user);
        user.addAccount(account);
        return account;
    }

    public void transfer(BankAccount source, BankAccount target, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) >= 0) { // source( отправитель) вызывает метод гет баланс и после сравнивает его с суммой что хотят перевести
            source.withdraw(amount); // отправитель отправляет
            target.deposit(amount); // получатель получает
            Transaction transaction = new Transaction(amount, TransactionType.TRANSFER, source, target); // мы создаем новую транзакцию
            source.addTransaction(transaction); // добавляем транзакцию отправителю
            target.addTransaction(transaction); // добавляем транзакцию получателю
        } else {
            System.out.println("Грошей на такие мувы не хватает ");
        }
    }

    public List<Transaction> getTransactionHistory(BankAccount account) {
        return account.getTransactions();
    }

    public BigDecimal getTotalBalance(User user) {
        BigDecimal total = BigDecimal.ZERO; // мы создаем переменную тотал и задаем что деняг нет
        for (BankAccount account : user.getAccounts()) { // user.getAccounts() возвращает список всех банковских счетов пользователя , и мы каждому из них проходимся
            total = total.add(account.getBalance());  // к изначальному тоталу ноль мы прибавляем деньги с каждого счета
        }
        return total;
    }
}

