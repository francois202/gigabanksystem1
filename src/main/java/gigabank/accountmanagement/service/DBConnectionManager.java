package gigabank.accountmanagement.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@Service
public class DBConnectionManager {
    private final DataSource dataSource;

    public DBConnectionManager() {
        Properties props = new Properties();
        loadProperties(props);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        config.addDataSourceProperty("maximumPoolSize", 10); // Настраиваем пул
        config.addDataSourceProperty("minimumIdle", 5);
        this.dataSource = new HikariDataSource(config);
    }

    private void loadProperties(Properties props) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IOException("Файл db.properties не найден");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки db.properties", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void disconnect() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    public void executeUpdate(String sql) {
        executeUpdateInTransaction(sql);
    }

    public void executeUpdateInTransaction(String... sqlStatements) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
                System.out.println("SQL-команда выполнена: " + sql + " в " + new java.util.Date());
            }

            conn.commit();
            System.out.println("Транзакция успешно завершена в " + new java.util.Date());
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Транзакция откатана из-за ошибки: " + e.getMessage() + " в " + new java.util.Date());
                } catch (SQLException ex) {
                    throw new RuntimeException("Ошибка отката транзакции: " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Ошибка выполнения SQL-запроса: " + e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка закрытия Statement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка закрытия соединения: " + e.getMessage());
                }
            }
        }
    }
}