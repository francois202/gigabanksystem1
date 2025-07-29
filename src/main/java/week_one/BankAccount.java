package week_one;

import lombok.Getter;
import lombok.Setter;
import week_two.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class BankAccount {

    private final String accountNumber;
    private  BigDecimal balance;
    private final User owner;
    private List<Transaction> transactions;

    public BankAccount(String accountNumber, User owner) { // конструктор
        this.accountNumber = accountNumber;
        this.balance = BigDecimal.ZERO; // задаю изначальный баланс 0
        this.owner = owner;
        this.transactions = new ArrayList<>(); // создаю пустой список в котором будут храниться транзакции
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount); // создал метод депозит который пополняет баланс add(amount)
        addTransaction(new Transaction(amount, TransactionType.DEPOSIT, this, null)); // и записывает эту транзакцию
    }

    public void withdraw(BigDecimal amount) { // метод на вывод деняг
        if (balance.subtract(amount).compareTo(BigDecimal.ZERO) <= 0) { // subtract это вычитание, из баланса вычитаем сумму которую переводим и сравниваем что остаток больше или равен нулю
            balance = balance.subtract(amount); // деньги отправляются
            addTransaction(new Transaction(amount, TransactionType.WITHDRAWAL, this, null)); // транзакция записывается
        } else {
            System.out.println("Грошей не хватает ");
        }
    }

    public void addTransaction(Transaction transaction) { // используем войд потому что нам надо просто изменить состояние внутри объекта
        transactions.add(transaction); // transactionS это список всех транзакций transactioN это именно эта транзакция (название берется из параметров метода я могу назвать ее по любому)
    }
}

