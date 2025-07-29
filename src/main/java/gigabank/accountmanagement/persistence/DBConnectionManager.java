package gigabank.accountmanagement.persistence;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Service
public class DBConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
    private static final String DB_PROPERTIES_FILE = "db.properties";
    private DataSource dataSource;
    private Properties props;
    private volatile Connection connection;

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
            synchronized (this) {
                this.connection = dataSource.getConnection();
                logger.info("HikariDataSource инициализирован и установлено соединение с помощью URL-адреса JDBC: {}", props.getProperty("db.url"));
            }
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

    public synchronized Connection obtainConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                logger.warn("Соединение закрыто или не инициализировано");
                connection = dataSource.getConnection();
                logger.debug("Новое соединение с базой данных получено");
            }
            return connection;
        } catch (SQLException e) {
            logger.error("Ошибка получения соединения: {} в {}", e.getMessage(), LocalDateTime.now());
            throw new RuntimeException("Ошибка получения соединения", e);
        }
    }

    public void executeUpdateInTransaction(List<SqlQuery> queries) {
        Connection localConn = null;
        try {
            localConn = obtainConnection();
            localConn.setAutoCommit(false);
            for (SqlQuery query : queries) {
                try (PreparedStatement stmt = localConn.prepareStatement(query.sql())) {
                    List<Object> parameters = query.parameters();
                    for (int i = 0; i < parameters.size(); i++) {
                        if (parameters.get(i) instanceof BigDecimal) {
                            stmt.setBigDecimal(i + 1, (BigDecimal) parameters.get(i));
                        } else {
                            stmt.setObject(i + 1, parameters.get(i));
                        }
                    }
                    stmt.executeUpdate();
                    logger.info("SQL-команда выполнена: {} в {}", query.sql(), LocalDateTime.now());
                }
            }
            localConn.commit();
            logger.info("Транзакция успешно завершена в {}", LocalDateTime.now());
        } catch (SQLException e) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.rollback();
                    logger.error("Транзакция откатана из-за ошибки: {} в {}", e.getMessage(), LocalDateTime.now());
                }
            } catch (SQLException ex) {
                logger.error("Ошибка отката транзакции: {}", ex.getMessage());
                throw new RuntimeException("Ошибка отката транзакции", ex);
            }
            logger.error("Ошибка выполнения SQL-запроса: {}", e.getMessage());
            throw new RuntimeException("Ошибка выполнения SQL-запроса", e);
        } finally {
            if (localConn != null) {
                try {
                    synchronized (this) {
                        if (localConn == connection && !localConn.isClosed()) {
                            localConn.close();
                            connection = null;
                            logger.debug("Соединение закрыто и поле connection занулено");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("Ошибка закрытия соединения в executeUpdateInTransaction: {}", e.getMessage());
                }
            }
        }
    }

    @PreDestroy
    public void disconnect() {
        try {
            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    logger.info("Соединение с базой данных закрыто");
                }
                connection = null;
            }
        } catch (SQLException e) {
            logger.error("Ошибка закрытия соединения: {}", e.getMessage());
        }
    }
}