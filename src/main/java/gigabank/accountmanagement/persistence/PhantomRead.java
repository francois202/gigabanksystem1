package gigabank.accountmanagement.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PhantomRead {
    private static final Logger logger = LoggerFactory.getLogger(PhantomRead.class);
    private final DBConnectionManager dbManager;
    private final String accountId;

    public PhantomRead(DBConnectionManager dbManager, String accountId) {
        this.dbManager = dbManager;
        this.accountId = accountId;
    }

    public void transactionRead() {
        Connection conn = null;
        try {
            conn = dbManager.obtainConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            BigDecimal initialBalance = getBalance(conn, accountId);
            logger.info("First reading of the balance for the account {}: {}", accountId, initialBalance);

            Thread.sleep(3000);

            BigDecimal updatedBalance = getBalance(conn, accountId);
            logger.info("Second reading of the balance for the account {}: {}", accountId, updatedBalance);

            if (!initialBalance.equals(updatedBalance)) {
                logger.warn("Phantom reading anomaly detected: balance changed from {} to {}", initialBalance, updatedBalance);
            } else {
                logger.info("Phantom reading anomaly not detected: balance remains {}", initialBalance);
            }

            conn.commit();
        } catch (InterruptedException e) {
            logger.error("Прерывание потока чтения: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            logger.error("Ошибка SQL при чтении: {}", e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Ошибка отката транзакции: {}", ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Ошибка закрытия соединения: {}", e.getMessage());
                }
            }
        }
    }

    public void transactionWrite() {
        Connection conn = null;
        try {
            conn = dbManager.obtainConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            Thread.sleep(1000);

            BigDecimal changeAmount = new BigDecimal("50.00");
            String updateSql = "UPDATE bankaccount SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setBigDecimal(1, changeAmount);
                updateStmt.setString(2, accountId);
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    logger.info("Account balance {} changed to the amount {}", accountId, changeAmount);

                    String transactionSql = "INSERT INTO transaction (id, user_id, amount, type, date) " +
                            "SELECT ?, user_id, ?, 'DEPOSIT', ? FROM bankaccount WHERE id = ?";
                    String transId = java.util.UUID.randomUUID().toString();
                    try (PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {
                        transStmt.setString(1, transId);
                        transStmt.setBigDecimal(2, changeAmount);
                        transStmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
                        transStmt.setString(4, accountId);
                        transStmt.executeUpdate();
                        logger.info("Транзакция добавлена: id={}, amount={}, type=DEPOSIT", transId, changeAmount);
                    }
                } else {
                    logger.warn("Счёт {} не найден для изменения", accountId);
                }
            }

            conn.commit();
        } catch (InterruptedException e) {
            logger.error("Прерывание потока записи: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            logger.error("Ошибка SQL при записи: {}", e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Ошибка отката транзакции: {}", ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Ошибка закрытия соединения: {}", e.getMessage());
                }
            }
        }
    }

    private static BigDecimal getBalance(Connection conn, String accountId) throws SQLException {
        String sql = "SELECT balance FROM bankaccount WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                throw new SQLException("Счёт не найден");
            }
        }
    }

    public void startPhantomReadDemo() {
        new Thread(this::transactionRead).start();
        new Thread(this::transactionWrite).start();
    }
}
