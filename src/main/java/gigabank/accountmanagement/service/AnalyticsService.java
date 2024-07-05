package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Сервис предоставляет аналитику по операциям пользователей
 */
public class AnalyticsService {
    /**
     * Вывод суммы потраченных средств на категорию за последний месяц
     * @param bankAccount - счет
     * @param category - категория
     */
    public BigDecimal getMonthlySpendingByCategory(BankAccount bankAccount, String category){
        return null;
    }

    /**
     * Вывод суммы потраченных средств на n категорий за последний месяц
     * со всех счетов пользователя
     *
     * @param user - пользователь
     * @param categories - категории
     * @return мапа категория - сумма потраченных средств
     */
    public Map<String, BigDecimal> getMonthlySpendingByCategories(User user, Set<String> categories){
        return null;
    }

    /**
     * Вывод платежных операций по всем счетам и по всем категориям от наибольшей к наименьшей
     * @param user - пользователь
     * @return мапа категория - все операции совершенные по ней
     */
    public TreeMap<String, List<Transaction>> getTransactionHistorySortedByAmount(User user){
        return null;
    }
}
