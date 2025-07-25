package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.TransactionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Service
public class AnalyticServiceBDImpl {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticServiceBDImpl.class);
    private final DBConnectionManager dbManager;

    public AnalyticServiceBDImpl(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    public BigDecimal getMonthlySpendingByCategory(String userId, String category) {
        String sql = "SELECT SUM(amount) FROM transaction t " +
                "JOIN bankaccount ba ON t.user_id = ba.user_id " +
                "WHERE t.type = 'PAYMENT' AND t.category = ? AND t.date >= ? AND ba.user_id = ?";
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        try (Connection conn = dbManager.obtainConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            stmt.setTimestamp(2, Timestamp.valueOf(oneMonthAgo));
            stmt.setString(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal amount = rs.getBigDecimal(1);
                    logger.info("Сумма потрачено на категорию {} за последний месяц для user_id {}: {}", category, userId, amount);
                    return amount != null ? amount : BigDecimal.ZERO;
                }
            }
            logger.warn("Нет данных по категории {} для user_id {}", category, userId);
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            logger.error("Ошибка получения ежемесячных трат: {}", e.getMessage());
            throw new RuntimeException("Ошибка аналитики", e);
        }
    }

    public Map<String, BigDecimal> getMonthlySpendingByCategories(String userId, Set<String> categories) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (categories == null || categories.isEmpty()) return result;

        String sql = "SELECT category, SUM(amount) FROM transaction t " +
                "JOIN bankaccount ba ON t.user_id = ba.user_id " +
                "WHERE t.type = 'PAYMENT' AND t.date >= ? AND ba.user_id = ? AND t.category IN (" +
                String.join(",", Collections.nCopies(categories.size(), "?")) + ") " +
                "GROUP BY category";
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        try (Connection conn = dbManager.obtainConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(oneMonthAgo));
            stmt.setString(2, userId);
            int index = 3;
            for (String category : categories) {
                stmt.setString(index++, category);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("category"), rs.getBigDecimal("sum"));
                }
            }
            logger.info("Сумма трат по категориям для user_id {}: {}", userId, result);
            return result;
        } catch (SQLException e) {
            logger.error("Ошибка получения трат по категориям: {}", e.getMessage());
            throw new RuntimeException("Ошибка аналитики", e);
        }
    }

    public LinkedHashMap<String, List<TransactionDTO>> getTransactionHistorySortedByAmount(String userId) {
        LinkedHashMap<String, List<TransactionDTO>> categorizedTransactions = new LinkedHashMap<>();
        String sql = "SELECT t.id, t.user_id, t.amount, t.type, t.date, t.source, t.target, t.category " +
                "FROM transaction t JOIN bankaccount ba ON t.user_id = ba.user_id " +
                "WHERE t.type = 'PAYMENT' AND ba.user_id = ?";

        try (Connection conn = dbManager.obtainConnection();
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
                    categorizedTransactions.computeIfAbsent(rs.getString("category"), k -> new ArrayList<>()).add(transaction);
                }
            }

            // Сортировка транзакций по сумме внутри каждой категории
            categorizedTransactions.forEach((category, transactions) ->
                    transactions.sort(Comparator.comparing(TransactionDTO::getAmount))
            );
            logger.info("История транзакций по категориям для user_id {}: {}", userId, categorizedTransactions);
            return categorizedTransactions;
        } catch (SQLException e) {
            logger.error("Ошибка получения истории транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка аналитики", e);
        }
    }

    public List<TransactionDTO> getLastNTransaction(String userId, int n) {
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM transaction " +
                "WHERE user_id = ? ORDER BY date DESC LIMIT ?";
        List<TransactionDTO> transactions = new ArrayList<>();

        try (Connection conn = dbManager.obtainConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setInt(2, n);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new TransactionDTO(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("type"),
                            rs.getTimestamp("date"),
                            rs.getString("source"),
                            rs.getString("target")
                    ));
                }
            }
            logger.info("Последние {} транзакций для user_id {}: {}", n, userId, transactions);
            return transactions;
        } catch (SQLException e) {
            logger.error("Ошибка получения последних транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка аналитики", e);
        }
    }

    public PriorityQueue<TransactionDTO> getTopNLargestTransactions(String userId, int n) {
        String sql = "SELECT id, user_id, amount, type, date, source, target FROM transaction " +
                "WHERE user_id = ? AND type = 'PAYMENT' ORDER BY amount DESC LIMIT ?";
        PriorityQueue<TransactionDTO> result = new PriorityQueue<>(Comparator.comparing(TransactionDTO::getAmount).reversed());

        try (Connection conn = dbManager.obtainConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setInt(2, n);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.offer(new TransactionDTO(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("type"),
                            rs.getTimestamp("date"),
                            rs.getString("source"),
                            rs.getString("target")
                    ));
                }
            }
            logger.info("Топ-{} крупнейших транзакций для user_id {}: {}", n, userId, result);
            return result;
        } catch (SQLException e) {
            logger.error("Ошибка получения топ-транзакций: {}", e.getMessage());
            throw new RuntimeException("Ошибка аналитики", e);
        }
    }
}