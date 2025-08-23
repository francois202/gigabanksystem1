package gigabank.accountmanagement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTables {
    public static void initializeDatabase() {
        Statement statement = null;

        try {
            Connection connection = DBConnectionManager.getInstance().getConnection();
            statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS bank_service.app_user (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "phone VARCHAR(20))"
            );

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS bank_service.bank_account (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "account_number VARCHAR(50), " +
                    "user_id VARCHAR(50) NOT NULL, " +
                    "balance DECIMAL(15,2) DEFAULT 0.00, " +
                    "currency VARCHAR(3), " +
                    "FOREIGN KEY (user_id) REFERENCES bank_service.app_user(id) ON DELETE CASCADE)"
            );

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS bank_service.app_transaction (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "amount DECIMAL(15,2) DEFAULT 0.00, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "source VARCHAR(50) DEFAULT NULL, " +
                    "target VARCHAR(50) DEFAULT NULL, " +
                    "FOREIGN KEY (source) REFERENCES bank_service.app_user(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (target) REFERENCES bank_service.app_user(id) ON DELETE SET NULL)"
            );
            addTestUsers(DBConnectionManager.getInstance().getConnection());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addTestUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM bank_service.bank_account");
        statement.executeUpdate("DELETE FROM bank_service.app_user");
        statement.close();

        PreparedStatement userStatement = connection.prepareStatement(
                "INSERT INTO bank_service.app_user (id, name, email, phone) VALUES (?, ?, ?, ?)");

        userStatement.setString(1, "user1");
        userStatement.setString(2, "Иван Иванов");
        userStatement.setString(3, "ivan@example.com");
        userStatement.setString(4, "+79161234567");
        userStatement.executeUpdate();

        userStatement.setString(1, "user2");
        userStatement.setString(2, "Петр Петров");
        userStatement.setString(3, "petr@example.com");
        userStatement.setString(4, "+79167654321");
        userStatement.executeUpdate();
        userStatement.close();

        PreparedStatement accountStatement = connection.prepareStatement(
                "INSERT INTO bank_service.bank_account (id, account_number, user_id, balance) VALUES (?, ?, ?, ?)");

        accountStatement.setString(1, "1");
        accountStatement.setString(2, "ACC1001");
        accountStatement.setString(3, "user1");
        accountStatement.setDouble(4, 1500.50);
        accountStatement.executeUpdate();

        accountStatement.setString(1, "2");
        accountStatement.setString(2, "ACC1002");
        accountStatement.setString(3, "user1");
        accountStatement.setDouble(4, 500.00);
        accountStatement.executeUpdate();

        accountStatement.setString(1, "3");
        accountStatement.setString(2, "ACC2001");
        accountStatement.setString(3, "user2");
        accountStatement.setDouble(4, 3000.75);
        accountStatement.executeUpdate();

        accountStatement.setString(1, "4");
        accountStatement.setString(2, "ACC2002");
        accountStatement.setString(3, "user2");
        accountStatement.setDouble(4, 1200.00);
        accountStatement.executeUpdate();

        accountStatement.close();
    }
}
