package gigabank.accountmanagement.hibernate;

import org.hibernate.Session;

public class TransactionDAO {

    public void save(TransactionEntity transaction) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(transaction);
            session.getTransaction().commit();
        }
    }

    // Остальные методы ...

}
