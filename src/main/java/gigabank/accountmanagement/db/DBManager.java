package gigabank.accountmanagement.db;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM bank_service.app_user";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getString("id"));
                user.setFirstName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPhoneNumber(resultSet.getString("phone"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить пользователей", e);
        }
        return users;
    }

    public void addUser(User user) {
        String sql = "INSERT INTO bank_service.app_user (id, name, email, phone) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getPhoneNumber());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Не удалось добавить пользователя, ни одна строка не была изменена");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при добавлении пользователя: " + e.getMessage(), e);
        }
    }

    public void addBankAccount(BankAccount bankAccount) {
        String sql = "INSERT INTO bank_service.bank_account (account_number, user_id, balance) VALUES (?, ?, ?)";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, bankAccount.getId());
            preparedStatement.setString(2, bankAccount.getOwner().getId());
            preparedStatement.setString(3, bankAccount.getBalance().toString());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Не удалось добавить счет, ни одна строка не была изменена");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении нового счета пользователя: " + e.getMessage(), e);
        }
    }

    public void deleteUser(User user) {
        String sql = "DELETE FROM bank_service.app_user WHERE id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getId());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Не удалось найти пользователя");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении пользователя: " + e.getMessage(), e);
        }
    }

    public void updateBalance(String accountNumber, BigDecimal newBalance) {
        String sql = "UPDATE bank_service.bank_account SET balance = ? WHERE account_number = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBigDecimal(1, newBalance);
            preparedStatement.setString(2, accountNumber);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Счет с номером " + accountNumber + " не найден");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении баланса: " + e.getMessage(), e);
        }
    }

    public void addTransaction(Transaction transaction) {
        String sql = "INSERT INTO bank_service.app_transaction (id, amount, type, date, source, target) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, transaction.getId());
            preparedStatement.setBigDecimal(2, transaction.getValue());
            preparedStatement.setString(3, String.valueOf(transaction.getType()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(transaction.getCreatedDate()));
            preparedStatement.setString(5, null);
            preparedStatement.setString(6, null);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("Не удалось сохранить транзакцию");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении транзакции: " + e.getMessage(), e);
        }
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM bank_service.transaction ORDER BY date DESC";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Transaction transaction = Transaction.builder()
                        .id(resultSet.getString("id"))
                        .value(resultSet.getBigDecimal("amount"))
                        .type(TransactionType.valueOf(resultSet.getString("type")))
                        .createdDate(resultSet.getTimestamp("date") != null ? resultSet.getTimestamp("date").toLocalDateTime() : null)
                        .build();

                transactions.add(transaction);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить транзакции", e);
        }
        return transactions;
    }

    public Transaction getTransaction(String transactionId) {
        String sql = "SELECT * FROM bank_service.transaction WHERE id = ?";

        try (Connection connection = DBConnectionManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, transactionId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Transaction transaction = Transaction.builder()
                        .id(resultSet.getString("id"))
                        .value(resultSet.getBigDecimal("amount"))
                        .type(TransactionType.valueOf(resultSet.getString("type")))
                        .createdDate(resultSet.getTimestamp("date") != null ? resultSet.getTimestamp("date").toLocalDateTime() : null)
                        .build();

                return transaction;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении транзакции: " + e.getMessage(), e);
        }
    }
}
