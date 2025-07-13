package gigabank.accountmanagement;

import gigabank.accountmanagement.service.DBConnectionManager;

public class Main {
    public static void main(String[] args) {
        DBConnectionManager dbManager = DBConnectionManager.getInstance();

        // Проверка соединения
        if (dbManager.getConnection() != null) {
            System.out.println("Connection is active");
        }

        // Закрытие соединения
        dbManager.disconnect();
    }
}