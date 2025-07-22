package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.BankAccountDTO;
import gigabank.accountmanagement.dto.TransactionDTO;
import gigabank.accountmanagement.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DBManager {
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    private final DBConnectionManager dbManager;

    private static final String INSERT_USER_SQL = "INSERT INTO \"users\" (user_id, username, email, phone) VALUES (?, ?, ?, ?)";
    private static final String SELECT_USERS_SQL = "SELECT user_id, username, email, phone FROM \"users\"";
    private static final String INSERT_BANK_ACCOUNT_SQL = "INSERT INTO \"bankaccount\" (id, number, user_id, balance) VALUES (?, ?, ?, ?)";
    private static final String DELETE_BANK_ACCOUNTS_SQL = "DELETE FROM \"bankaccount\" WHERE user_id = ?";
    private static final String DELETE_USER_SQL = "DELETE FROM \"users\" WHERE user_id = ?";
    private static final String UPDATE_BALANCE_SQL = "UPDATE \"bankaccount\" SET balance = ? WHERE id = ?";
    private static final String INSERT_TRANSACTION_SQL = "INSERT INTO \"transaction\" (id, user_id, amount, type, date, source, target) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_TRANSACTIONS_SQL = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\"";
    private static final String SELECT_TRANSACTION_BY_ID_SQL = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\" WHERE id = ?";
    private static final String SELECT_ACCOUNTS_BY_USER_SQL = "SELECT id, number, user_id, balance FROM \"bankaccount\" WHERE user_id = ?";
    private static final String SELECT_TRANSACTIONS_BY_DATE_RANGE_SQL = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\" WHERE date BETWEEN ? AND ?";

    DBManager(DBConnectionManager dbManager) {
        if (dbManager == null) {
            throw new IllegalArgumentException("DBConnectionManager cannot be null");
        }
        this.dbManager = dbManager;
    }

    public void addUser(String username, String email, String phone) throws SQLException {
        String userId = UUID.randomUUID().toString();
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(INSERT_USER_SQL)) {
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.executeUpdate();
            logger.info("Пользователь добавлен: {}, user_id: {} в {}", username, userId, new java.util.Date());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                throw new SQLException("Пользователь " + userId + " уже существует", e);
            }
            logger.error("Ошибка добавления пользователя: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка добавления пользователя", e);
        }
    }

    public List<UserDTO> getUsers() {
        List<UserDTO> users = new ArrayList<>();
        try (Statement stmt = dbManager.obtainConnection().createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_USERS_SQL)) {
            while (rs.next()) {
                UserDTO user = new UserDTO(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
                users.add(user);
            }
            logger.info("Получено {} пользователей в {}", users.size(), new java.util.Date());
            return users;
        } catch (SQLException e) {
            logger.error("Ошибка получения пользователей: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка получения пользователей", e);
        }
    }

    public void addBankAccount(String userId, BigDecimal initialBalance) throws SQLException {
        String accountId = UUID.randomUUID().toString();
        String accountNumber = "ACC_" + UUID.randomUUID().toString();
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(INSERT_BANK_ACCOUNT_SQL)) {
            stmt.setString(1, accountId);
            stmt.setString(2, accountNumber);
            stmt.setString(3, userId);
            stmt.setBigDecimal(4, initialBalance);
            stmt.executeUpdate();
            logger.info("Счёт добавлен: {}, баланс: {} в {}",accountId, initialBalance, new java.util.Date());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                throw new SQLException("Банковский счёт с ID " + userId + " уже существует", e);
            }
            logger.error("Ошибка добавления счёта: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка добавления счёта", e);
        }
    }

    public void deleteUser(String userId) throws SQLException {
        List<Object[]> queries = List.of(
                new Object[]{DELETE_BANK_ACCOUNTS_SQL, userId},
                new Object[]{DELETE_USER_SQL, userId}
        );
        try {
            dbManager.executeUpdateInTransaction(queries);
            logger.info("Пользователь и его счёта удалены: {} в {}", userId, new java.util.Date());
        } catch (SQLException e) {
            logger.error("Ошибка удаления пользователя: {} в {}", e.getMessage(), new java.util.Date());
            throw e;
        }
    }

    public void updateBalance(String accountId, BigDecimal newBalance) {
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(UPDATE_BALANCE_SQL)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setString(2, accountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Баланс обновлён для счёта: {}, новый баланс: {} в {}", accountId, newBalance, new java.util.Date());
            } else {
                logger.warn("Счёт не найден: {} в {}", accountId, new java.util.Date());
            }
        } catch (SQLException e) {
            logger.error("Ошибка обновления баланса: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка обновления баланса", e);
        }
    }

    public void addTransaction(String transactionId, String userId, BigDecimal amount, String type, Timestamp date, String source, String target) throws SQLException {
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(INSERT_TRANSACTION_SQL)) {
            stmt.setString(1, transactionId);
            stmt.setString(2, userId);
            stmt.setBigDecimal(3, amount);
            stmt.setString(4, type);
            stmt.setTimestamp(5, date);
            stmt.setString(6, source);
            stmt.setString(7, target);
            stmt.executeUpdate();
            logger.info("Транзакция добавлена: {}, сумма: {} в {}",transactionId, amount, new java.util.Date());
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                logger.error("Транзакция с ID {} уже существует: {}", transactionId, e.getMessage());
                throw new SQLException("Транзакция с ID " + transactionId + " уже существует", e);
            }
            logger.error("Ошибка добавления транзакции: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка добавления транзакции", e);
        }
    }

    public List<TransactionDTO> getTransactions() {
        List<TransactionDTO> transactions = new ArrayList<>();
        try (Statement stmt = dbManager.obtainConnection().createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_TRANSACTIONS_SQL)) {
            while (rs.next()) {
                TransactionDTO transaction = new TransactionDTO(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("type"),
                        rs.getTimestamp("date"),
                        rs.getString("source"),
                        rs.getString("target")
                );
                transactions.add(transaction);
            }
            logger.info("Получено {} транзакций в {}", transactions.size(), new java.util.Date());
            return transactions;
        } catch (SQLException e) {
            logger.error("Ошибка получения транзакций: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакций", e);
        }
    }

    public TransactionDTO getTransaction(String transactionId) {
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(SELECT_TRANSACTION_BY_ID_SQL)) {
            stmt.setString(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TransactionDTO transaction = new TransactionDTO(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("type"),
                            rs.getTimestamp("date"),
                            rs.getString("source"),
                            rs.getString("target")
                    );
                    logger.info("Транзакция найдена: {} в {}", transactionId, new java.util.Date());
                    return transaction;
                } else {
                    logger.warn("Транзакция не найдена: {} в {}", transactionId, new java.util.Date());
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка получения транзакции: {} в {}", e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакции", e);
        }
    }

    public List<BankAccountDTO> getAccountsByUser(String userId) {
        List<BankAccountDTO> accounts = new ArrayList<>();
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(SELECT_ACCOUNTS_BY_USER_SQL)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BankAccountDTO account = new BankAccountDTO(
                            rs.getString("id"),
                            rs.getString("number"),
                            rs.getString("user_id"),
                            rs.getBigDecimal("balance")
                    );
                    accounts.add(account);
                }
            }
            logger.info("Получено {} счетов для пользователя: {} в {}", accounts.size(), userId, new java.util.Date());
            return accounts;
        } catch (SQLException e) {
            logger.error("Ошибка получения счетов: {} в {}",e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка получения счетов", e);
        }
    }

    public List<TransactionDTO> getTransactionsByDateRange(Timestamp startDate, Timestamp endDate) {
        List<TransactionDTO> transactions = new ArrayList<>();
        try (PreparedStatement stmt = dbManager.obtainConnection().prepareStatement(SELECT_TRANSACTIONS_BY_DATE_RANGE_SQL)) {
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TransactionDTO transaction = new TransactionDTO(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("type"),
                            rs.getTimestamp("date"),
                            rs.getString("source"),
                            rs.getString("target")
                    );
                    transactions.add(transaction);
                }
            }
            logger.info("Получено {} транзакций за период с {} по {} в {}", transactions.size(), startDate, endDate, new java.util.Date());
            return transactions;
        } catch (SQLException e) {
            logger.error("Ошибка получения транзакций по дате: {} в {}",e.getMessage(), new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакций по дате", e);
        }
    }
}