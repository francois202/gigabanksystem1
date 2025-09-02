package gigabank.accountmanagement.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionManager {
    public static DBConnectionManager instance;
    Connection connection;
    Properties props;

    private DBConnectionManager() {
        loadProperties();
        connect();
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            props = new Properties();
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить настройки базы данных", e);
        }
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(
                            props.getProperty("db.url"),
                            props.getProperty("db.username"),
                            props.getProperty("db.password")
                    );

        }
        catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к базе данных", e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Не удалось получить статус соединения", e);
        }
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось закрыть соединение", e);
        }
    }
}
