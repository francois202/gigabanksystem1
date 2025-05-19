package gigabank.accountmanagement.hibernate;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;


public class UserDAO {

    public void save(UserEntity user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }
    }

    public UserEntity findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(UserEntity.class, id);
        }
    }

    // Остальные методы ...

    public List<UserEntity> findAll(int pageNumber, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserEntity> query = session.createQuery("from UserEntity order by id", UserEntity.class);
            query.setFirstResult((pageNumber - 1) * pageSize);
            /**
             * Изучить проблемы при работе с пагинацией.
             * Изучить подробно: Проблема «пропущенных» или «дублированных» записей
             *
             * Изучить Optimistic/Pessimistic Locking
             * Рассказать ментору
             * */
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    public long countAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return (Long) session.createQuery("select count(*) from UserEntity").uniqueResult();
        }
    }
}
