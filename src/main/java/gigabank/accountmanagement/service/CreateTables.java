package gigabank.accountmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CreateTables {
    private static final Logger logger = LoggerFactory.getLogger(CreateTables.class);
    private final DBConnectionManager dbManager;

    CreateTables(DBConnectionManager dbManager) {
        if (dbManager == null) {
            throw new IllegalArgumentException("DBConnectionManager не может быть null");
        }
        this.dbManager = dbManager;
    }

    public List<String> initializeDatabase() {
        List<Object[]> sqlStatements = Arrays.asList(
                new Object[]{
                        """
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(50) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    email VARCHAR(100) NOT NULL UNIQUE,
                    phone VARCHAR(15) UNIQUE
                )
                """
                },
                new Object[]{
                        """
                CREATE TABLE IF NOT EXISTS bankaccount (
                    id VARCHAR(50) PRIMARY KEY,
                    number VARCHAR(100) NOT NULL UNIQUE,
                    user_id VARCHAR(50) NOT NULL,
                    balance DECIMAL(15, 2) DEFAULT 0.00,
                    FOREIGN KEY (user_id) REFERENCES "users"(user_id)
                )
                """
                },
                new Object[]{
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
                """
                }
        );

        try {
            dbManager.executeUpdateInTransaction(sqlStatements);
            logger.info("Таблицы базы данных успешно созданы в {}", new java.util.Date());
            return initializeSampleData();
        } catch (SQLException e) {
            logger.error("Ошибка создания таблиц: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка создания таблиц", e);
        }
    }

    private List<String> initializeSampleData() {
        List<String> createdAccountIds = new ArrayList<>();
        List<Object[]> insertStatements = new ArrayList<>();

        String userId1 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "users" (user_id, username, email, phone)
            VALUES (?, ?, ?, ?)
            """,
                userId1, "Джон Крукс", "john.kruks@gmail.com", "+9586478555"
        });

        String accountId1 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "bankaccount" (id, number, user_id, balance)
            VALUES (?, ?, ?, ?)
            """,
                accountId1, "ACC_" + UUID.randomUUID().toString(), userId1, new BigDecimal("1000.00")
        });

        String accountId2 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "bankaccount" (id, number, user_id, balance)
            VALUES (?, ?, ?, ?)
            """,
                accountId2, "ACC_" + UUID.randomUUID().toString(), userId1, new BigDecimal("500.00")
        });

        String userId2 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "users" (user_id, username, email, phone)
            VALUES (?, ?, ?, ?)
            """,
                userId2, "Ivan Orlov", "orlov.ivan@mail.ru", "+79127334728"
        });

        String accountId3 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "bankaccount" (id, number, user_id, balance)
            VALUES (?, ?, ?, ?)
            """,
                accountId3, "ACC_" + UUID.randomUUID().toString(), userId2, new BigDecimal("2000.00")
        });

        String accountId4 = UUID.randomUUID().toString();
        insertStatements.add(new Object[]{
                """
            INSERT INTO "bankaccount" (id, number, user_id, balance)
            VALUES (?, ?, ?, ?)
            """,
                accountId4, "ACC_" + UUID.randomUUID().toString(), userId2, new BigDecimal("750.00")
        });

        try {
            dbManager.executeUpdateInTransaction(insertStatements);
            createdAccountIds.addAll(Arrays.asList(accountId1, accountId2, accountId3, accountId4));
            logger.info("Образовательные данные успешно добавлены, созданы счета: {} в {}", createdAccountIds, new java.util.Date());
            return createdAccountIds;
        } catch (SQLException e) {
            logger.error("Ошибка добавления образовательных данных: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка добавления образовательных данных", e);
        }
    }
}