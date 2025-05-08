package gigabank.accountmanagement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTables {
    public static void initializeDatabase() {
        Statement statement = null;

        try {
            Connection conn = DBConnectionManager.getInstance().getConnection();
            statement = conn.createStatement();

            // Создание таблицы User
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS account_management.User (" +
                    "user_id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "phone VARCHAR(20))");

            // Создание таблицы BankAccount
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS account_management.BankAccount (" +
                    "account_number VARCHAR(50) PRIMARY KEY, " +
                    "user_id VARCHAR(50) NOT NULL, " +
                    "balance DECIMAL(15,2) DEFAULT 0.00, " +
                    "currency VARCHAR(3), " +
                    "FOREIGN KEY (user_id) REFERENCES account_management.User(user_id))");

            // Добавление тестовых пользователей
            addTestUsers(conn);

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addTestUsers(Connection conn) throws SQLException {
        // Очистка таблиц перед добавлением
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM account_management.BankAccount");
        stmt.executeUpdate("DELETE FROM account_management.User");
        stmt.close();

        // Добавление пользователей
        PreparedStatement userStmt = conn.prepareStatement(
                "INSERT INTO account_management.User (user_id, name, email, phone) VALUES (?, ?, ?, ?)");

        // Первый пользователь
        userStmt.setString(1, "user1");
        userStmt.setString(2, "Иван Иванов");
        userStmt.setString(3, "ivan@example.com");
        userStmt.setString(4, "+79161234567");
        userStmt.executeUpdate();

        // Второй пользователь
        userStmt.setString(1, "user2");
        userStmt.setString(2, "Петр Петров");
        userStmt.setString(3, "petr@example.com");
        userStmt.setString(4, "+79167654321");
        userStmt.executeUpdate();
        userStmt.close();

        // Добавление банковских счетов
        PreparedStatement accountStmt = conn.prepareStatement(
                "INSERT INTO account_management.BankAccount (account_number, user_id, balance, currency) VALUES (?, ?, ?, ?)");

        // Счета для первого пользователя
        accountStmt.setString(1, "ACC1001");
        accountStmt.setString(2, "user1");
        accountStmt.setDouble(3, 1500.50);
        accountStmt.setString(4, "RUB");
        accountStmt.executeUpdate();

        accountStmt.setString(1, "ACC1002");
        accountStmt.setString(2, "user1");
        accountStmt.setDouble(3, 500.00);
        accountStmt.setString(4, "USD");
        accountStmt.executeUpdate();

        // Счета для второго пользователя
        accountStmt.setString(1, "ACC2001");
        accountStmt.setString(2, "user2");
        accountStmt.setDouble(3, 3000.75);
        accountStmt.setString(4, "RUB");
        accountStmt.executeUpdate();

        accountStmt.setString(1, "ACC2002");
        accountStmt.setString(2, "user2");
        accountStmt.setDouble(3, 1200.00);
        accountStmt.setString(4, "EUR");
        accountStmt.executeUpdate();

        accountStmt.close();
    }
}