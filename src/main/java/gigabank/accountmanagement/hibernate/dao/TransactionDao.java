package gigabank.accountmanagement.hibernate.dao;

import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDao {

    public void save(TransactionEntity transactionEntity) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(transactionEntity);
            transaction.commit();
        }
    }

    public List<TransactionEntity> findByAccountId(Long accountId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM TransactionEntity t WHERE t.bankAccountEntity.id = :accountId";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("accountId", accountId);
            return query.getResultList();
        }
    }

    public List<TransactionEntity> findByUserId(Long userId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM TransactionEntity t " +
                    "JOIN t.bankAccountEntity b " +
                    "JOIN b.owner u " +
                    "WHERE u.id = :userId";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }

    public List<TransactionEntity> findByDate(LocalDateTime date) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM TransactionEntity t " +
                    "WHERE DATE(t.createdDate) = DATE(:date) " +
                    "ORDER BY t.createdDate";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("date", date);
            return query.getResultList();
        }
    }

    public List<TransactionEntity> findByAmount(BigDecimal amount) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t FROM TransactionEntity t WHERE t.value = :amount";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("amount", amount);
            return query.getResultList();
        }
    }
}