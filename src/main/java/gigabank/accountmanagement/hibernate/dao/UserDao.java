package gigabank.accountmanagement.hibernate.dao;

import gigabank.accountmanagement.entity.UserEntity;
import gigabank.accountmanagement.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UserDao {
    public void save(UserEntity userEntity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(userEntity);
            transaction.commit();
        }
    }

    public UserEntity findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(UserEntity.class, id);
        }
    }

    public List<UserEntity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<UserEntity> query = session.createQuery("FROM UserEntity", UserEntity.class);
            return query.getResultList();
        }
    }

    public void update(UserEntity userEntity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(userEntity);
            transaction.commit();
        }
    }

    public void delete(UserEntity userEntity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            UserEntity userEntityToDelete = findById(userEntity.getId());

            if (userEntityToDelete != null) {
                session.remove(userEntityToDelete);
            }
            transaction.commit();
        }
    }
}
