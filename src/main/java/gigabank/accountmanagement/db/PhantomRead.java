package gigabank.accountmanagement.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static gigabank.accountmanagement.db.DBConnectionManager.instance;

public class PhantomRead {

    public static DBConnectionManager getPhantomReadTestInstance () {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }


    public static void main(String[] args) {
        // Запуск двух транзакций в разных потоках
        new Thread(PhantomRead::transactionRead).start();
        new Thread(PhantomRead::transactionWrite).start();
    }

    // Транзакция чтения (демонстрирует фантомное чтение)
    private static void transactionRead() {
        Connection conn = getPhantomReadTestInstance().getConnection();
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Уровень изоляции, допускающий фантомное чтение

            System.out.println("[READ] Транзакция чтения начата");

            // Первое чтение баланса
            int balance1 = getBalance(conn, "ACC1001");
            System.out.println("[READ] Первое чтение баланса: " + balance1);

            // Искусственная задержка между чтениями
            Thread.sleep(3000);

            // Второе чтение баланса (здесь может появиться фантомное чтение)
            int balance2 = getBalance(conn, "ACC1001");
            System.out.println("[READ] Второе чтение баланса: " + balance2);

            if (balance1 != balance2) {
                System.out.println("[READ] Обнаружено фантомное чтение! Баланс изменился между двумя чтениями.");
            }

            conn.commit();
            System.out.println("[READ] Транзакция чтения завершена");
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Транзакция записи (изменяет баланс)
    private static void transactionWrite() {
        Connection conn = getPhantomReadTestInstance().getConnection();

        try {
            // Задержка, чтобы гарантировать, что первое чтение уже произошло
            Thread.sleep(1000);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            System.out.println("[WRITE] Транзакция записи начата");

            // Изменение баланса
            updateBalance(conn, "ACC1001", 200);
            System.out.println("[WRITE] Баланс пользователя изменен на 200");

            conn.commit();
            System.out.println("[WRITE] Транзакция записи завершена");
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Вспомогательный метод для получения баланса
    private static int getBalance(Connection conn, String accountNumber) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT balance FROM account_management.bankaccount WHERE account_number = ?"
        );
        stmt.setString(1, accountNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("balance");
        }
        return 0;
    }

    // Вспомогательный метод для обновления баланса
    private static void updateBalance(Connection conn, String accountNumber, int newBalance) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "UPDATE account_management.bankaccount SET balance = ? WHERE account_number = ?"
        );
        stmt.setInt(1, newBalance);
        stmt.setString(2, accountNumber);
        stmt.executeUpdate();
    }

    /**
     * Вывод в консоль должен быть примерно таким:
     *
     * Тестовые данные инициализированы: создан пользователь с ID=1 и балансом=100
     * [READ] Транзакция чтения начата
     * [READ] Первое чтение баланса: 100
     * [WRITE] Транзакция записи начата
     * [WRITE] Баланс пользователя изменен на 200
     * [WRITE] Транзакция записи завершена
     * [READ] Второе чтение баланса: 200
     * [READ] Обнаружено фантомное чтение! Баланс изменился между двумя чтениями.
     * [READ] Транзакция чтения завершена
     *
     * Нужно изменить уровень изоляции и посмотреть как отработает код.
     * Рассказать ментору зачем synchronized в методе:
     *
     *     public static synchronized DBConnectionManager getInstance() {
     *         if (instance == null) {
     *             instance = new DBConnectionManager();
     *         }
     *         return instance;
     *     }
     *
     * */
}

