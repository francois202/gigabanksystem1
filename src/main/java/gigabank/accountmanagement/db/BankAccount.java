package gigabank.accountmanagement.db;

import gigabank.accountmanagement.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankAccount {

    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        // указать <имя схемы>.<имя таблицы>
        String sql = "SELECT * FROM account_management.user";

        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getString("user_id"));
                user.setFirstName(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve users", e);
        }
        return users;
    }

    // Другие методы ...
}