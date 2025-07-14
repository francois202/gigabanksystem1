package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    //реализация данного Set - неизменяемый стрим, не допускающий передачу null значений
    public static final Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education");

    public Boolean isValidCategory(String category) {
        return category != null && TRANSACTION_CATEGORIES.contains(category);
    }

    public Set<String> validateCategories(Set<String> categories) {
        Set<String> validCategories = new HashSet<>();

        for (String category : categories) {
            if (isValidCategory(category)) {
                validCategories.add(category);
            }
        }
        return validCategories;
    }

    /**
     * Фильтрует транзакции пользователя с использованием Predicate.
     *
     * @param user      - пользователь
     * @param predicate - условие фильтрации
     * @return список транзакций, удовлетворяющих условию
     */
    public List<Transaction> filterTransactions(User user, Predicate<Transaction> predicate) {
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует транзакции пользователя с использованием Function.
     *
     * @param user     - пользователь
     * @param function - функция преобразования
     * @return список строковых представлений транзакций
     */
    public List<String> transformTransactions(User user, Function<Transaction, String> function) {
        if (user == null) {
            return Collections.emptyList();
        }
        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .map(function)
                .collect(Collectors.toList());

    }

    /**
     * Обрабатывает транзакции пользователя с использованием Consumer.
     *
     * @param user     - пользователь
     * @param consumer - функция обработки
     */
    public void processTransactions(User user, Consumer<Transaction> consumer) {
        if (user == null) {
            return;
        }

        user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .forEach(consumer);
    }

    /**
     * Создаёт список транзакций с использованием Supplier.
     *
     * @param supplier - поставщик
     * @return созданный список транзакций
     */
    public List<Transaction> createTransactionList(Supplier<List<Transaction>> supplier) {
        return supplier.get();
    }

    public List<Transaction> mergeTransactionList(List<Transaction> list1, List<Transaction> list2, BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> biFunction) {
        return biFunction.apply(list1, list2);
    }
}
