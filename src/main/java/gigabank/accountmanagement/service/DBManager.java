package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.BankAccountDTO;
import gigabank.accountmanagement.dto.TransactionDTO;
import gigabank.accountmanagement.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DBManager {
    private final DBConnectionManager dbManager;

    public DBManager(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    public void addUser(String username, String email, String phone) {
        String userId = UUID.randomUUID().toString();
        String sql = "INSERT INTO \"users\" (user_id, username, email, phone) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.executeUpdate();
            System.out.println("Пользователь добавлен: " + username + ", user_id: " + userId + " в " + new java.util.Date());
        } catch (SQLException e) {
            System.err.println("Ошибка добавления пользователя: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка добавления пользователя", e);
        }
    }

    public List<UserDTO> getUsers() {
        List<UserDTO> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email, phone FROM \"users\"";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UserDTO user = new UserDTO(
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("phone")
                );
                users.add(user);
            }
            System.out.println("Получено " + users.size() + " пользователей в " + new java.util.Date());
            return users;
        } catch (SQLException e) {
            System.err.println("Ошибка получения пользователей: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка получения пользователей", e);
        }
    }

    public void addBankAccount(String userId, BigDecimal initialBalance) {
        String accountId = UUID.randomUUID().toString();
        String accountNumber = "ACC_" + UUID.randomUUID().toString();
        String sql = "INSERT INTO \"bankaccount\" (id, number, user_id, balance) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, accountId);
            stmt.setString(2, accountNumber);
            stmt.setString(3, userId);
            stmt.setBigDecimal(4, initialBalance);
            stmt.executeUpdate();
            System.out.println("Счёт добавлен: " + accountId + ", баланс: " + initialBalance + " в " + new java.util.Date());
        } catch (SQLException e) {
            System.err.println("Ошибка добавления счёта: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка добавления счёта", e);
        }
    }

    public void deleteUser(String userId) {
        String[] sqlStatements = {
                "DELETE FROM \"bankaccount\" WHERE user_id = ?",
                "DELETE FROM \"users\" WHERE user_id = ?"
        };
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            for (String sql : sqlStatements) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, userId);
                    stmt.executeUpdate();
                }
            }
            conn.commit();
            System.out.println("Пользователь и его счёты удалены: " + userId + " в " + new java.util.Date());
        } catch (SQLException e) {
            try {
                if (dbManager.getConnection() != null) {
                    dbManager.getConnection().rollback();
                    System.err.println("Транзакция откатана из-за ошибки: " + e.getMessage() + " в " + new java.util.Date());
                }
            } catch (SQLException ex) {
                System.err.println("Ошибка отката транзакции: " + ex.getMessage());
            }
            throw new RuntimeException("Ошибка удаления пользователя", e);
        }
    }

    public void updateBalance(String accountId, BigDecimal newBalance) {
        String sql = "UPDATE \"bankaccount\" SET balance = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setString(2, accountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Баланс обновлён для счёта: " + accountId + ", новый баланс: " + newBalance + " в " + new java.util.Date());
            } else {
                System.out.println("Счёт не найден: " + accountId + " в " + new java.util.Date());
            }
        } catch (SQLException e) {
            System.err.println("Ошибка обновления баланса: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка обновления баланса", e);
        }
    }

    public void addTransaction(String transactionId, String userId, BigDecimal amount, String type, Timestamp date, String source, String target) {
        String sql = "INSERT INTO \"transaction\" (id, user_id, amount, type, date, source, target) VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, transactionId);
            stmt.setString(2, userId);
            stmt.setBigDecimal(3, amount);
            stmt.setString(4, type);
            stmt.setTimestamp(5, date);
            stmt.setString(6, source);
            stmt.setString(7, target);
            stmt.executeUpdate();
            System.out.println("Транзакция добавлена: " + transactionId + ", сумма: " + amount + " в " + new java.util.Date());
        } catch (SQLException e) {
            System.err.println("Ошибка добавления транзакции: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка добавления транзакции", e);
        }
    }

    public List<TransactionDTO> getTransactions() {
        List<TransactionDTO> transactions = new ArrayList<>();
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\"";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
            System.out.println("Получено " + transactions.size() + " транзакций в " + new java.util.Date());
            return transactions;
        } catch (SQLException e) {
            System.err.println("Ошибка получения транзакций: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакций", e);
        }
    }

    public TransactionDTO getTransaction(String transactionId) {
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\" WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
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
                    System.out.println("Транзакция найдена: " + transactionId + " в " + new java.util.Date());
                    return transaction;
                } else {
                    System.out.println("Транзакция не найдена: " + transactionId + " в " + new java.util.Date());
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения транзакции: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакции", e);
        }
    }

    public List<BankAccountDTO> getAccountsByUser(String userId) {
        List<BankAccountDTO> accounts = new ArrayList<>();
        String sql = "SELECT id, number, user_id, balance FROM \"bankAccount\" WHERE user_id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
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
            System.out.println("Получено " + accounts.size() + " счетов для пользователя " + userId + " в " + new java.util.Date());
            return accounts;
        } catch (SQLException e) {
            System.err.println("Ошибка получения счетов: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка получения счетов", e);
        }
    }

    public List<TransactionDTO> getTransactionsByDateRange(Timestamp startDate, Timestamp endDate) {
        List<TransactionDTO> transactions = new ArrayList<>();
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM \"transaction\" WHERE date BETWEEN ? AND ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
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
            System.out.println("Получено " + transactions.size() + " транзакций за период с " + startDate + " по " + endDate + " в " + new java.util.Date());
            return transactions;
        } catch (SQLException e) {
            System.err.println("Ошибка получения транзакций по дате: " + e.getMessage() + " в " + new java.util.Date());
            throw new RuntimeException("Ошибка получения транзакций по дате", e);
        }
    }
}