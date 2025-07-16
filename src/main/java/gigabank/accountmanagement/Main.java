package gigabank.accountmanagement;

import gigabank.accountmanagement.service.DBConnectionManager;

import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        DBConnectionManager dbManager = DBConnectionManager.getInstance();

        String sql = """
                SELECT ALL FROM 
                """;

        try {
            // Выполняем SQL-запрос для создания схемы
            dbManager.executeUpdate(sql);

            // Проверка соединения
            if (dbManager.getConnection() != null) {
                System.out.println("Соединение активно в " + new java.util.Date());
            }
        } catch (RuntimeException e) {
            System.err.println("Ошибка в " + new java.util.Date() + ": " + e.getMessage());
        }

        // Проверка соединения
        if (dbManager.getConnection() != null) {
            System.out.println("Соединение активно");
        }

        // Закрытие соединения
        dbManager.disconnect();
    }
}