package gigabank.accountmanagement.hibernate;

import java.util.List;

public class PaginationExample {
    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAO();

            // Пример пагинации пользователей
            int userPage = 1;
            int pageSize = 5;

            List<UserEntity> users = userDAO.findAll(userPage, pageSize);
            long totalUsers = userDAO.countAllUsers();
            System.out.println("Пользователи (страница " + userPage + "):");
            users.forEach(u -> System.out.println(u.getName()));
            System.out.println("Всего пользователей: " + totalUsers);
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
