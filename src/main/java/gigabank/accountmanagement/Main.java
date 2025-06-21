package gigabank.accountmanagement;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.AnalyticsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.LoggerFactory;



public class Main {
    public static void main(String[] args) {
        // Проверка работы jul-to-slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        System.out.println("SLF4J LoggerFactory: " + LoggerFactory.getILoggerFactory().getClass().getName());
        // Создаем транзакции
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("t1", BigDecimal.valueOf(100), TransactionType.PAYMENT, "Food", LocalDateTime.now().minusDays(10)));
        transactions.add(new Transaction("t2", BigDecimal.valueOf(200), TransactionType.PAYMENT, "Transport", LocalDateTime.now().minusDays(20)));
        transactions.add(new Transaction("t3", BigDecimal.valueOf(50), TransactionType.DEPOSIT, "Salary", LocalDateTime.now().minusDays(30)));

        // Создаем банковский счет
        BankAccount ba = new BankAccount("ba1", transactions);

        // Создаем пользователя
        User user = new User();
        user.setId("u1");
        user.setFirstName("John");
        user.setLastName("Doe");
        List<BankAccount> bankAccounts = new ArrayList<>();
        bankAccounts.add(ba);
        user.setBankAccounts(bankAccounts);

        // Устанавливаем владельца для банковского счета
        ba.setOwner(user);

        // Создаем сервис аналитики
        AnalyticsService analyticsService = new AnalyticsService();

        // Вызываем аннотированные методы и проверяем логирование
        System.out.println("Calling getMonthlySpendingByCategory");
        BigDecimal spending = analyticsService.getMonthlySpendingByCategory(ba, "Food");
        System.out.println("Spending on Food: " + spending);

        System.out.println("Calling getTopNLargestTransactions");
        analyticsService.getTopNLargestTransactions(user, 2);

        System.out.println("Finished calling methods");
    }
}