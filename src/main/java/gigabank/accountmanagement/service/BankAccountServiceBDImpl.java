package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.TransactionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BankAccountServiceBDImpl {
    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceBDImpl.class);
    private final DBConnectionManager dbManager;

    public BankAccountServiceBDImpl(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    public void deposit(String accountId, BigDecimal amount) {
        String sql = "UPDATE bankaccount SET balance = balance + ? WHERE id = ?";
        String transactionSql = "INSERT INTO transaction (id, user_id, amount, type, date) " +
                "SELECT ?, user_id, ?, 'DEPOSIT', ? FROM bankaccount WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(sql);
             PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {

            conn.setAutoCommit(false);

            updateStmt.setBigDecimal(1, amount);
            updateStmt.setString(2, accountId);
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                String transId = UUID.randomUUID().toString();
                transStmt.setString(1, transId);
                transStmt.setBigDecimal(2, amount);
                transStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                transStmt.setString(4, accountId);
                transStmt.executeUpdate();
                logger.info("Пополнение счёта {} на сумму {} успешно", accountId, amount);
            } else {
                logger.warn("Счёт {} не найден для пополнения", accountId);
                throw new RuntimeException("Счёт не найден");
            }

            conn.commit();
        } catch (SQLException e) {
            logger.error("Ошибка пополнения счёта: {}", e.getMessage());
            throw new RuntimeException("Ошибка пополнения", e);
        }
    }

    public DBConnectionManager getDbManager() {
        return dbManager;
    }

    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        String checkBalanceSql = "SELECT balance, user_id FROM bankaccount WHERE id = ?";
        String updateFromSql = "UPDATE bankaccount SET balance = balance - ? WHERE id = ?";
        String updateToSql = "UPDATE bankaccount SET balance = balance + ? WHERE id = ?";
        String transactionFromSql = "INSERT INTO transaction (id, user_id, amount, type, date, target) " +
                "SELECT ?, user_id, ?, 'TRANSFER_OUT', ?, (SELECT user_id FROM bankaccount WHERE id = ?) FROM bankaccount WHERE id = ?";
        String transactionToSql = "INSERT INTO transaction (id, user_id, amount, type, date, source) " +
                "SELECT ?, user_id, ?, 'TRANSFER_IN', ?, (SELECT user_id FROM bankaccount WHERE id = ?) FROM bankaccount WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkBalanceSql);
             PreparedStatement updateFromStmt = conn.prepareStatement(updateFromSql);
             PreparedStatement updateToStmt = conn.prepareStatement(updateToSql);
             PreparedStatement transFromStmt = conn.prepareStatement(transactionFromSql);
             PreparedStatement transToStmt = conn.prepareStatement(transactionToSql)) {

            conn.setAutoCommit(false);

            checkStmt.setString(1, fromAccountId);
            BigDecimal fromBalance = null;
            String fromUserId = null;
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    fromBalance = rs.getBigDecimal("balance");
                    fromUserId = rs.getString("user_id");
                }
            }
            if (fromBalance == null || fromBalance.compareTo(amount) < 0) {
                logger.warn("Недостаточно средств на счёте {} для перевода {}", fromAccountId, amount);
                throw new RuntimeException("Недостаточно средств");
            }

            String toUserId = null;
            checkStmt.setString(1, toAccountId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    toUserId = rs.getString("user_id");
                } else {
                    logger.warn("Счёт {} не найден", toAccountId);
                    throw new RuntimeException("Счёт получателя не найден");
                }
            }
            updateFromStmt.setBigDecimal(1, amount);
            updateFromStmt.setString(2, fromAccountId);
            updateFromStmt.executeUpdate();

            updateToStmt.setBigDecimal(1, amount);
            updateToStmt.setString(2, toAccountId);
            updateToStmt.executeUpdate();

            String transIdFrom = UUID.randomUUID().toString();
            transFromStmt.setString(1, transIdFrom);
            transFromStmt.setBigDecimal(2, amount.negate());
            transFromStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            transFromStmt.setString(4, toUserId);
            transFromStmt.setString(5, fromAccountId);
            transFromStmt.executeUpdate();

            String transIdTo = UUID.randomUUID().toString();
            transToStmt.setString(1, transIdTo);
            transToStmt.setBigDecimal(2, amount);
            transToStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            transToStmt.setString(4, fromUserId);
            transToStmt.setString(5, toAccountId);
            transToStmt.executeUpdate();

            logger.info("Перевод суммы {} с счёта {} на счёт {} выполнен", amount, fromAccountId, toAccountId);

            conn.commit();
        } catch (SQLException e) {
            logger.error("Ошибка перевода: {}", e.getMessage());
            throw new RuntimeException("Ошибка перевода", e);
        }
    }

    public void deleteAccount(String accountId) {
        String deleteSql = "DELETE FROM bankaccount WHERE id = ?";
        String transactionSql = "INSERT INTO transaction (id, user_id, amount, type, date) " +
                "SELECT ?, user_id, 0, 'ACCOUNT_DELETED', ? FROM bankaccount WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
             PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {

            conn.setAutoCommit(false);

            deleteStmt.setString(1, accountId);
            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) {
                String transId = UUID.randomUUID().toString();
                transStmt.setString(1, transId);
                transStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                transStmt.setString(3, accountId);
                transStmt.executeUpdate();
                logger.info("Счёт {} удалён", accountId);
            } else {
                logger.warn("Счёт {} не найден для удаления", accountId);
                throw new RuntimeException("Счёт не найден");
            }

            conn.commit();
        } catch (SQLException e) {
            logger.error("Ошибка удаления счёта: {}", e.getMessage());
            throw new RuntimeException("Ошибка удаления", e);
        }
    }

    public List<TransactionDTO> getTransactionsByUser(String userId) {
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM transaction WHERE user_id = ?";
        List<TransactionDTO> transactions = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
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
            return transactions;
        } catch (SQLException e) {
            logger.error("Ошибка получения транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения транзакций", e);
        }
    }
}