package gigabank.accountmanagement.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CreateTables {
    private static final Logger logger = LoggerFactory.getLogger(CreateTables.class);
    private final DBConnectionManager dbManager;
    private static final String CREATE_USERS_TABLE =
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id VARCHAR(50) PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                email VARCHAR(100) NOT NULL UNIQUE,
                phone VARCHAR(15) UNIQUE
            )
            """;

    private static final String CREATE_BANKACCOUNT_TABLE =
            """
            CREATE TABLE IF NOT EXISTS bankaccount (
                id VARCHAR(50) PRIMARY KEY,
                number VARCHAR(100) NOT NULL UNIQUE,
                user_id VARCHAR(50) NOT NULL,
                balance DECIMAL(15, 2) DEFAULT 0.00,
                FOREIGN KEY (user_id) REFERENCES "users"(user_id)
            )
            """;

    private static final String CREATE_TRANSACTION_TABLE =
            """
                CREATE TABLE IF NOT EXISTS transaction (
                    id VARCHAR(50) PRIMARY KEY,
                    user_id VARCHAR(50),
                    amount DECIMAL(15, 2) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    date TIMESTAMP,
                    source VARCHAR(50),
                    target VARCHAR(50),
                    FOREIGN KEY (user_id) REFERENCES "users"(user_id),
                    FOREIGN KEY (source) REFERENCES "users"(user_id),
                    FOREIGN KEY (target) REFERENCES "users"(user_id)
                )
                """;

    private static final String INSERT_USER_SQL =
            """
            INSERT INTO "users" (user_id, username, email, phone)
            VALUES (?, ?, ?, ?)
            """;

    private static final String INSERT_BANKACCOUNT_SQL =
            """
            INSERT INTO "bankaccount" (id, number, user_id, balance)
            VALUES (?, ?, ?, ?)
            """;

    CreateTables(DBConnectionManager dbManager) {
        if (dbManager == null) {
            throw new IllegalArgumentException("DBConnectionManager не может быть null");
        }
        this.dbManager = dbManager;
    }

    public List<String> initializeDatabase() {
        List<SqlQuery> sqlStatements = Arrays.asList(
                new SqlQuery(CREATE_USERS_TABLE),
                new SqlQuery(CREATE_BANKACCOUNT_TABLE),
                new SqlQuery(CREATE_TRANSACTION_TABLE)
        );

        try {
            dbManager.executeUpdateInTransaction(sqlStatements);
            logger.info("Таблицы базы данных успешно созданы в {}", LocalDateTime.now());
            return initializeSampleData();
        } catch (RuntimeException e) {
            logger.error("Ошибка создания таблиц: {} в {}", e.getMessage(), LocalDateTime.now());
            throw new RuntimeException("Ошибка создания таблиц", e);
        }
    }

    private List<String> initializeSampleData() {
        List<String> createdAccountIds = new ArrayList<>();
        List<SqlQuery> insertStatements = new ArrayList<>();

        String userId1 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_USER_SQL,
                userId1, "Джон Крукс", "john.kruks@gmail.com", "+9586478555"
        ));

        String accountId1 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_BANKACCOUNT_SQL,
                accountId1, "ACC_" + UUID.randomUUID().toString(), userId1, new BigDecimal("1000.00")
        ));

        String accountId2 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_BANKACCOUNT_SQL,
                accountId2, "ACC_" + UUID.randomUUID().toString(), userId1, new BigDecimal("500.00")
        ));

        String userId2 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_USER_SQL,
                userId2, "Ivan Orlov", "orlov.ivan@mail.ru", "+79127334728"
        ));

        String accountId3 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_BANKACCOUNT_SQL,
                accountId3, "ACC_" + UUID.randomUUID().toString(), userId2, new BigDecimal("2000.00")
        ));

        String accountId4 = UUID.randomUUID().toString();
        insertStatements.add(new SqlQuery(
                INSERT_BANKACCOUNT_SQL,
                accountId4, "ACC_" + UUID.randomUUID().toString(), userId2, new BigDecimal("750.00")
        ));

        try {
            dbManager.executeUpdateInTransaction(insertStatements);
            createdAccountIds.addAll(Arrays.asList(accountId1, accountId2, accountId3, accountId4));
            logger.info("Образовательные данные успешно добавлены, созданы счета: {} в {}", createdAccountIds, LocalDateTime.now());
            return createdAccountIds;
        } catch (RuntimeException e) {
            logger.error("Ошибка добавления образовательных данных: {} в {}", e.getMessage(), LocalDateTime.now());
            throw new RuntimeException("Ошибка добавления образовательных данных", e);
        }
    }
}