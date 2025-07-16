package gigabank.accountmanagement.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnectionManager {
    private static DBConnectionManager instance;
    private Connection connection;
    private Properties props;

    private DBConnectionManager() {
        props = new Properties();
        loadProperties();
        connect();
    }

    public static synchronized DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IOException("Файл db.properties не найден");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки db.properties", e);
        }
    }

    public void connect() {
        try {
            // Загрузка JDBC-драйвера
            Class.forName(props.getProperty("db.driver"));
            connection = DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );
            System.out.println("Успешно подключен к базе данных");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Драйвер JDBC не найден", e);
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось подключиться к базе данных", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Соединение с базой данных закрыто");
            } catch (SQLException e) {
                throw new RuntimeException("Не удалось закрыть соединение с базой данных", e);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeUpdate(String sql) {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("SQL-команда выполнена: " + sql);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка выполнения SQL-запроса: " + e.getMessage(), e);
        }
    }
}