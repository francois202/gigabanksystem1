package gigabank.accountmanagement.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.math.BigDecimal;

public class BankAccountDAO {

    public void save(BankAccountEntity account) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(account);
            transaction.commit();
        }
    }

    public void deposit(Long accountId, BigDecimal amount) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            BankAccountEntity account = session.get(BankAccountEntity.class, accountId);
            if (account != null) {
                account.setBalance(account.getBalance().add(amount));

                TransactionEntity transactionEntity = new TransactionEntity();
                transactionEntity.setAmount(amount);
                transactionEntity.setDescription("Deposit");
                transactionEntity.setBankAccount(account);

                session.persist(transactionEntity);
            }
            transaction.commit();
        }
    }

    // Остальные методы ...

}
