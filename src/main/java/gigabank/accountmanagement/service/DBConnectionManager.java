package gigabank.accountmanagement.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Service
public class DBConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    private static final String DB_PROPERTIES_FILE = "db.properties";
    private DataSource dataSource;
    private Properties props;
    private Connection connection;

    public DBConnectionManager() {
    }


    @PostConstruct
    public void initConnection() {
        this.props = new Properties();
        loadProperties(props);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        config.addDataSourceProperty("maximumPoolSize", 10);
        config.addDataSourceProperty("minimumIdle", 5);
        this.dataSource = new HikariDataSource(config);
        try {
            this.connection = dataSource.getConnection();
            logger.info("HikariDataSource инициализирован и установлено соединение с помощью URL-адреса JDBC: {}", props.getProperty("db.url"));
        } catch (SQLException e) {
            logger.error("Ошибка установления соединения с базой данных: {}", e.getMessage());
            throw new RuntimeException("Ошибка установления соединения", e);
        }
    }

    private void loadProperties(Properties props) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (input == null) {
                logger.error("Файл {} не найден", DB_PROPERTIES_FILE);
                throw new IOException("Файл " + DB_PROPERTIES_FILE + " не найден");
            }
            props.load(input);
            logger.info("Свойства базы данных успешно загружены из {}", DB_PROPERTIES_FILE);
        } catch (IOException e) {
            logger.error("Ошибка загрузки {}: {}", DB_PROPERTIES_FILE, e.getMessage());
            throw new RuntimeException("Ошибка загрузки " + DB_PROPERTIES_FILE, e);
        }
    }

    public Connection obtainConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.warn("Соединение закрыто или не инициализировано");
            connection = dataSource.getConnection();
            logger.debug("Новое соединение с базой данных получено");
        }
        return connection;
    }

    public void executeUpdateInTransaction(List<Object[]> queries) throws SQLException {
        try (Connection conn = obtainConnection()) {
            conn.setAutoCommit(false);
            for (Object[] query : queries) {
                if (query.length < 1 || !(query[0] instanceof String)) {
                    throw new IllegalArgumentException("Каждый запрос должен содержать SQL-строку как первый элемент");
                }
                String sql = (String) query[0];
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (int i = 1; i < query.length; i++) {
                        if (query[i] instanceof BigDecimal) {
                            stmt.setBigDecimal(i, (BigDecimal) query[i]); // Явно устанавливаем BigDecimal
                        } else {
                            stmt.setObject(i, query[i]);
                        }
                    }
                    stmt.executeUpdate();
                    logger.info("SQL-команда выполнена: {} в {}", sql, new java.util.Date());
                }
            }
            conn.commit();
            logger.info("Транзакция успешно завершена в {}", new java.util.Date());
        } catch (SQLException e) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.rollback();
                    logger.error("Транзакция откатана из-за ошибки: {} в {}", e.getMessage(), new java.util.Date());
                }
            } catch (SQLException ex) {
                logger.error("Ошибка отката транзакции: {}", ex.getMessage());
                throw ex;
            }
            logger.error("Ошибка выполнения SQL-запроса: {}", e.getMessage());
            throw e;
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            logger.error("Ошибка закрытия соединения: {}", e.getMessage());
        }
        ((HikariDataSource) dataSource).close();
        logger.info("HikariDataSource закрыт");
    }
}