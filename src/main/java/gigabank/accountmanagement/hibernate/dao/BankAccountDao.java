package gigabank.accountmanagement.hibernate.dao;

import gigabank.accountmanagement.entity.BankAccountEntity;
import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BankAccountDao {
    public void save(BankAccountEntity account) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(account);
            transaction.commit();
        }
    }

    public BankAccountEntity findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(BankAccountEntity.class, id);
        }
    }

    public List<BankAccountEntity> findByUserId(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BankAccountEntity> query = session.createQuery(
                    "FROM BankAccountEntity WHERE owner = :userId", BankAccountEntity.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }

    public void deposit(Long accountId, BigDecimal amount) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            BankAccountEntity account = session.get(BankAccountEntity.class, accountId);
            if (account != null) {
                account.setBalance(account.getBalance().add(amount));

                TransactionEntity transactionEntity = new TransactionEntity();
                transactionEntity.setValue(amount);
                transactionEntity.setType(TransactionType.DEPOSIT);
                transactionEntity.setCreatedDate(LocalDateTime.now());
                transactionEntity.setBankAccountEntity(account);

                session.persist(transactionEntity);
            }

            transaction.commit();
        }
    }

    public void withdraw(Long accountId, BigDecimal amount) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            BankAccountEntity account = session.get(BankAccountEntity.class, accountId);
            if (account != null && account.getBalance().compareTo(amount) >= 0) {
                account.setBalance(account.getBalance().subtract(amount));

                TransactionEntity transactionEntity = new TransactionEntity();
                transactionEntity.setValue(amount);
                transactionEntity.setType(TransactionType.WITHDRAWAL);
                transactionEntity.setCategory("OTHER");
                transactionEntity.setCreatedDate(LocalDateTime.now());
                transactionEntity.setBankAccountEntity(account);

                session.persist(transactionEntity);
            } else {
                throw new RuntimeException("Недостаточно средств на счете");
            }
            transaction.commit();
        }
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            BankAccountEntity fromAccount = session.get(BankAccountEntity.class, fromAccountId);
            BankAccountEntity toAccount = session.get(BankAccountEntity.class, toAccountId);

            if (fromAccount != null && toAccount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                if (fromAccount.getBalance().compareTo(amount) >= 0) {
                    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));

                    toAccount.setBalance(toAccount.getBalance().add(amount));

                    TransactionEntity withdrawalTransaction = new TransactionEntity();
                    withdrawalTransaction.setValue(amount);
                    withdrawalTransaction.setType(TransactionType.WITHDRAWAL);
                    withdrawalTransaction.setCreatedDate(LocalDateTime.now());
                    withdrawalTransaction.setBankAccountEntity(fromAccount);
                    withdrawalTransaction.setTargetAccount(toAccount.toString());

                    TransactionEntity depositTransaction = new TransactionEntity();
                    depositTransaction.setValue(amount);
                    depositTransaction.setType(TransactionType.DEPOSIT);
                    depositTransaction.setCreatedDate(LocalDateTime.now());
                    depositTransaction.setBankAccountEntity(toAccount);
                    depositTransaction.setSourceAccount(toAccount.toString());

                    session.persist(withdrawalTransaction);
                    session.persist(depositTransaction);
                } else {
                    throw new RuntimeException("Недостаточно средств для перевода");
                }
            }

            transaction.commit();
        }
    }

    public void delete(Long accountId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            BankAccountEntity account = session.find(BankAccountEntity.class, accountId);
            if (account != null && account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                session.remove(account);
            } else {
                throw new RuntimeException("Нельзя удалить счет с ненулевым балансом");
            }
            transaction.commit();
        }
    }
}
