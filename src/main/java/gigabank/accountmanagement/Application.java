package gigabank.accountmanagement;

import gigabank.accountmanagement.service.BankAccountServiceBDImpl;
import gigabank.accountmanagement.service.CreateTables;
import gigabank.accountmanagement.service.DBConnectionManager;
import gigabank.accountmanagement.service.PhantomRead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner init(CreateTables createTables, BankAccountServiceBDImpl bankAccountService, DBConnectionManager dbManager) {
        return args -> {
            try {
                // Инициализация базы данных и получение списка созданных accountId
                List<String> accountIds = createTables.initializeDatabase();
                logger.info("Инициализация базы данных завершена");

                if (accountIds.size() >= 3) { // Убедимся, что есть как минимум три счёта
                    String accountId1 = accountIds.get(0); // Первый счёт
                    BigDecimal depositAmount = new BigDecimal("100.00");
                    bankAccountService.deposit(accountId1, depositAmount);
                    logger.info("Пополнение баланса счёта {} на сумму {}", accountId1, depositAmount);

                    String fromAccountId = accountId1;
                    String toAccountId = accountIds.get(1); // Второй счёт
                    BigDecimal transferAmount = new BigDecimal("50.00");
                    bankAccountService.transfer(fromAccountId, toAccountId, transferAmount);
                    logger.info("Перевод суммы {} с счёта {} на счёт {}", transferAmount, fromAccountId, toAccountId);

                    String accountToDelete = accountIds.get(2); // Третий счёт
                    bankAccountService.deleteAccount(accountToDelete);
                    logger.info("Счёт {} удалён", accountToDelete);

                    String userId = getUserIdForAccount(accountId1, bankAccountService);
                    logger.info("Транзакции пользователя с user_id {}:", userId);
                    bankAccountService.getTransactionsByUser(userId).forEach(transaction ->
                            logger.info("Транзакция: id={}, amount={}, type={}, date={}",
                                    transaction.getId(), transaction.getAmount(), transaction.getType(), transaction.getDate())
                    );

                    // Запуск демонстрации фантомного чтения с использованием accountId1
                    PhantomRead phantomRead = new PhantomRead(dbManager, accountId1);
                    phantomRead.startPhantomReadDemo();
                } else {
                    logger.warn("Недостаточно счетов для выполнения операций. Создано счетов: {}", accountIds.size());
                    // Попытка получить существующие счета из базы данных
                    accountIds = getExistingAccountIds(bankAccountService);
                    if (accountIds.size() >= 3) {
                        String accountId1 = accountIds.get(0);
                        BigDecimal depositAmount = new BigDecimal("100.00");
                        bankAccountService.deposit(accountId1, depositAmount);
                        logger.info("Пополнение баланса счёта {} на сумму {}", accountId1, depositAmount);

                        String fromAccountId = accountId1;
                        String toAccountId = accountIds.get(1);
                        BigDecimal transferAmount = new BigDecimal("50.00");
                        bankAccountService.transfer(fromAccountId, toAccountId, transferAmount);
                        logger.info("Перевод суммы {} с счёта {} на счёт {}", transferAmount, fromAccountId, toAccountId);

                        String accountToDelete = accountIds.get(2);
                        bankAccountService.deleteAccount(accountToDelete);
                        logger.info("Счёт {} удалён", accountToDelete);

                        String userId = getUserIdForAccount(accountId1, bankAccountService);
                        logger.info("Транзакции пользователя с user_id {}:", userId);
                        bankAccountService.getTransactionsByUser(userId).forEach(transaction ->
                                logger.info("Транзакция: id={}, amount={}, type={}, date={}",
                                        transaction.getId(), transaction.getAmount(), transaction.getType(), transaction.getDate())
                        );

                        // Запуск демонстрации фантомного чтения с использованием accountId1
                        PhantomRead phantomRead = new PhantomRead(dbManager, accountId1);
                        phantomRead.startPhantomReadDemo();
                    } else {
                        logger.error("Недостаточно существующих счетов для выполнения операций. Найдено счетов: {}", accountIds.size());
                        throw new RuntimeException("Недостаточно счетов в базе данных");
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка выполнения операций: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    private String getUserIdForAccount(String accountId, BankAccountServiceBDImpl bankAccountService) {
        String sql = "SELECT user_id FROM bankaccount WHERE id = ?";
        try (Connection conn = bankAccountService.getDbManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_id");
                }
                throw new RuntimeException("Счёт " + accountId + " не найден");
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения user_id для счёта {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Ошибка получения user_id", e);
        }
    }

    private List<String> getExistingAccountIds(BankAccountServiceBDImpl bankAccountService) {
        String sql = "SELECT id FROM bankaccount";
        List<String> accountIds = new ArrayList<>();
        try (Connection conn = bankAccountService.getDbManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accountIds.add(rs.getString("id"));
            }
            logger.info("Найдено существующих счетов: {}", accountIds.size());
            return accountIds;
        } catch (SQLException e) {
            logger.error("Ошибка получения существующих accountId: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения accountId", e);
        }
    }
}