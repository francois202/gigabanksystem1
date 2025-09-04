package gigabank.accountmanagement;

import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.UserEntity;
import gigabank.accountmanagement.hibernate.HibernateUtil;
import gigabank.accountmanagement.hibernate.dao.BankAccountDao;
import gigabank.accountmanagement.hibernate.dao.TransactionDao;
import gigabank.accountmanagement.hibernate.dao.UserDao;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        UserDao userDAO = new UserDao();
        BankAccountDao bankAccountDAO = new BankAccountDao();
        TransactionDao transactionDAO = new TransactionDao();

        UserEntity userEntity1 = new UserEntity();
        userEntity1.setName("Иван Иванов");
        userEntity1.setEmail("ivan12@example.com");
        userEntity1.setPhoneNumber("+79000000002");
        userDAO.save(userEntity1);
        userDAO.delete(userEntity1);

        BankAccountEntity account1 = new BankAccountEntity();

        account1.setAccountNumber("1234567892");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setOwner(userEntity1);
        bankAccountDAO.save(account1);
        System.out.println("Создан счет: " + account1.getAccountNumber());

        bankAccountDAO.deposit(account1.getId(), BigDecimal.valueOf(10000));
        System.out.println("Пополнение счета на 10000");

        UserEntity user2 = new UserEntity();
        user2.setName("Петр Петров");
        user2.setEmail("petr22@example.com");
        user2.setPhoneNumber("+79000000003");
        userDAO.save(user2);

        BankAccountEntity account2 = new BankAccountEntity();
        account2.setAccountNumber("98765432114");
        account2.setBalance(BigDecimal.valueOf(1000000000));
        account2.setOwner(user2);
        bankAccountDAO.save(account2);
        bankAccountDAO.deposit(account2.getId(), BigDecimal.valueOf(500));
        System.out.println("Создан второй пользователь и счет");

        HibernateUtil.close();
    }
}