package gigabank.accountmanagement.service;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CreateTables {
    private final DBConnectionManager dbManager;

    public CreateTables(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<String> initializeDatabase() {
        String[] sqlStatements = {
                "DROP TABLE IF EXISTS transaction",
                "DROP TABLE IF EXISTS bankaccount",
                "DROP TABLE IF EXISTS users",

                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id VARCHAR(50) PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "phone VARCHAR(15) UNIQUE)",

                "CREATE TABLE IF NOT EXISTS bankaccount (" +
                        "id VARCHAR(50) PRIMARY KEY, " +
                        "number VARCHAR(100) NOT NULL UNIQUE, " +
                        "user_id VARCHAR(50) NOT NULL, " +
                        "balance DECIMAL(15, 2) DEFAULT 0.00, " +
                        "FOREIGN KEY (user_id) REFERENCES \"users\"(user_id))",

                "CREATE TABLE IF NOT EXISTS transaction (" +
                        "id VARCHAR(50) PRIMARY KEY, " +
                        "user_id VARCHAR(50), " +
                        "amount DECIMAL(15, 2) NOT NULL, " +
                        "type VARCHAR(20) NOT NULL, " +
                        "date TIMESTAMP, " +
                        "source VARCHAR(50), " +
                        "target VARCHAR(50), " +
                        "FOREIGN KEY (user_id) REFERENCES \"users\"(user_id), " +
                        "FOREIGN KEY (source) REFERENCES \"users\"(user_id), " +
                        "FOREIGN KEY (target) REFERENCES \"users\"(user_id))"
        };

        dbManager.executeUpdateInTransaction(sqlStatements);
        return initializeSampleData();
    }

    private List<String> initializeSampleData() {
        List<String> createdAccountIds = new ArrayList<>();
        String[] insertStatements = {
                "INSERT INTO \"users\" (user_id, username, email, phone) VALUES (?, ?, ?, ?)",
                "INSERT INTO \"bankaccount\" (id, number, user_id, balance) VALUES (?, ?, ?, ?)"
        };

        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement accountStmt = null;

        try {
            conn = dbManager.getConnection();
            conn.setAutoCommit(false);

            userStmt = conn.prepareStatement(insertStatements[0]);
            accountStmt = conn.prepareStatement(insertStatements[1]);

            String userId1 = UUID.randomUUID().toString();
            userStmt.setString(1, userId1);
            userStmt.setString(2, "Джон Крукс");
            userStmt.setString(3, "john.kruks@gmail.com");
            userStmt.setString(4, "+9586478555");
            int userRows1 = userStmt.executeUpdate();
            System.out.println("Добавлен пользователь: " + userId1 + ", Джон Крукс, строк затронуто: " + userRows1);

            String accountId1 = UUID.randomUUID().toString();
            accountStmt.setString(1, accountId1);
            accountStmt.setString(2, "ACC_" + UUID.randomUUID().toString());
            accountStmt.setString(3, userId1);
            accountStmt.setBigDecimal(4, new java.math.BigDecimal("1000.00"));
            accountStmt.executeUpdate();
            System.out.println("Добавлен счёт: " + accountId1 + ", баланс: 1000.00");
            createdAccountIds.add(accountId1);

            String accountId2 = UUID.randomUUID().toString();
            accountStmt.setString(1, accountId2);
            accountStmt.setString(2, "ACC_" + UUID.randomUUID().toString());
            accountStmt.setString(3, userId1);
            accountStmt.setBigDecimal(4, new java.math.BigDecimal("500.00"));
            accountStmt.executeUpdate();
            System.out.println("Добавлен счёт: " + accountId2 + ", баланс: 500.00");
            createdAccountIds.add(accountId2);

            String userId2 = UUID.randomUUID().toString();
            userStmt.setString(1, userId2);
            userStmt.setString(2, "Ivan Orlov");
            userStmt.setString(3, "orlov.ivan@mail.ru");
            userStmt.setString(4, "+0987654321");
            int userRows2 = userStmt.executeUpdate();
            System.out.println("Добавлен пользователь: " + userId2 + ", Ivan Orlov, строк затронуто: " + userRows2);

            String accountId3 = UUID.randomUUID().toString();
            accountStmt.setString(1, accountId3);
            accountStmt.setString(2, "ACC_" + UUID.randomUUID().toString());
            accountStmt.setString(3, userId2);
            accountStmt.setBigDecimal(4, new java.math.BigDecimal("2000.00"));
            accountStmt.executeUpdate();
            System.out.println("Добавлен счёт: " + accountId3 + ", баланс: 2000.00");
            createdAccountIds.add(accountId3);

            String accountId4 = UUID.randomUUID().toString();
            accountStmt.setString(1, accountId4);
            accountStmt.setString(2, "ACC_" + UUID.randomUUID().toString());
            accountStmt.setString(3, userId2);
            accountStmt.setBigDecimal(4, new java.math.BigDecimal("750.00"));
            accountStmt.executeUpdate();
            System.out.println("Добавлен счёт: " + accountId4 + ", баланс: 750.00");
            createdAccountIds.add(accountId4);

            conn.commit();
            System.out.println("Образовательные данные успешно добавлены в " + new java.util.Date());
            return createdAccountIds;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Транзакция откатана из-за ошибки: " + e.getMessage() + " в " + new java.util.Date());
                } catch (SQLException ex) {
                    System.err.println("Ошибка отката транзакции: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Ошибка добавления данных: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Восстанавливаем autoCommit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка закрытия соединения: " + e.getMessage());
                }
            }
            if (userStmt != null) try {
                userStmt.close();
            } catch (SQLException e) {
                System.err.println("Ошибка закрытия userStmt: " + e.getMessage());
            }
            if (accountStmt != null) try {
                accountStmt.close();
            } catch (SQLException e) {
                System.err.println("Ошибка закрытия accountStmt: " + e.getMessage());
            }
        }
    }
}