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

    //почему здесь Set, а не List?
    public static Set<String> transactionCategories = Set.of(
            "Health", "Beauty", "Education");

    public boolean isValidCategories(String category){
        return category != null && transactionCategories.contains(category);
    }

    public Set<String> validateCategories (Set<String> categories) {
        Set<String> result = new HashSet<>();
        for (String category : categories) {
            if (isValidCategories(category))
                result.add(category);
        }
        return result;
    }

    /**
     * Фильтрует транзакции пользователя с использованием Predicate.
     *
     * @param user      - пользователь
     * @param predicate - условие фильтрации
     * @return список транзакций, удовлетворяющих условию
     */
    public List<Transaction> filterTransactions (User user, Predicate<Transaction> predicate){
        if (user == null)
            return Collections.EMPTY_LIST;

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     *  Преобразует транзакции пользователя с использованием Function.
     *
     * @param user - пользователь
     * @param function - функция преобразования
     * @return  список строковых представлений транзакций
     */
    public List<String> transformTransaction (User user, Function<Transaction, String> function){
        if (user == null)
            return Collections.emptyList();

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
    public void processTransactions (User user, Consumer<Transaction> consumer){
        if (user == null)
            return;
        user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .forEach(consumer);
    }

    /**
     * Создает список транзакций с использованием Supplier.
     *
     * @param supplier - источник
     * @return возвращает созданный список транзакций
     */
    public List<Transaction> createTrasactionList (Supplier<List<Transaction>> supplier){
        return supplier.get();
    }

    /**
     *  Объединяет списки транзакций в один список c использованием BiFunction.
     *
     * @param list1 - 1-й список транзакций
     * @param list2 - 2-й список транзакций
     * @param merger - функция объединения транзакций
     * @return возвращает объединённый список транзакций
     */
    public List<Transaction> mergeTransactionList (List<Transaction> list1, List<Transaction> list2, BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> merger){
        if (list1 == null || list2 == null)
            return Collections.emptyList();
        return merger.apply(list1, list2);
    }
}
