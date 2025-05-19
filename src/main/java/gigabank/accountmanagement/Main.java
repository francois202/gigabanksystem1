package gigabank.accountmanagement;

import gigabank.accountmanagement.hibernate.*;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        try {
            // Инициализация DAO
            UserDAO userDAO = new UserDAO();
            BankAccountDAO accountDAO = new BankAccountDAO();
            TransactionDAO transactionDAO = new TransactionDAO();

            // 1. Создание и сохранение пользователя
            UserEntity user1 = new UserEntity();
            user1.setName("Иван Иванов");
            user1.setEmail("ivan@example.com");
            userDAO.save(user1);
            System.out.println("Создан пользователь: " + user1.getName());

            // 2. Создание банковского счета для пользователя
            BankAccountEntity account1 = new BankAccountEntity();
            account1.setAccountNumber("1234567890");
            account1.setCurrency(BankAccountEntity.Currency.RUB);
            account1.setUser(user1);
            accountDAO.save(account1);
            System.out.println("Создан счет: " + account1.getAccountNumber());

            // 3. Пополнение счета
            accountDAO.deposit(account1.getId(), BigDecimal.valueOf(10000));
            System.out.println("Пополнение счета на 10000");

            // 4. Создание второго пользователя и счета
            UserEntity user2 = new UserEntity();
            user2.setName("Петр Петров");
            user2.setEmail("petr@example.com");
            userDAO.save(user2);

            BankAccountEntity account2 = new BankAccountEntity();
            account2.setAccountNumber("9876543210");
            account2.setCurrency(BankAccountEntity.Currency.USD);
            account2.setUser(user2);
            accountDAO.save(account2);
            accountDAO.deposit(account2.getId(), BigDecimal.valueOf(500));
            System.out.println("Создан второй пользователь и счет в USD");

        } finally {
            // Завершение работы Hibernate
            HibernateUtil.shutdown();
        }
    }
}