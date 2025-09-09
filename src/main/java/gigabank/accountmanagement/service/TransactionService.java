package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.TransactionEntity;
import gigabank.accountmanagement.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
@Service
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
     * @param userEntity      - пользователь
     * @param predicate - условие фильтрации
     * @return список транзакций, удовлетворяющих условию
     */
    public List<TransactionEntity> filterTransactions(UserEntity userEntity, Predicate<TransactionEntity> predicate) {
        if (userEntity == null) {
            return Collections.emptyList();
        }

        return userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует транзакции пользователя с использованием Function.
     *
     * @param userEntity     - пользователь
     * @param function - функция преобразования
     * @return список строковых представлений транзакций
     */
    public List<String> transformTransactions(UserEntity userEntity, Function<TransactionEntity, String> function) {
        if (userEntity == null) {
            return Collections.emptyList();
        }
        return userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .map(function)
                .collect(Collectors.toList());

    }

    /**
     * Обрабатывает транзакции пользователя с использованием Consumer.
     *
     * @param userEntity     - пользователь
     * @param consumer - функция обработки
     */
    public void processTransactions(UserEntity userEntity, Consumer<TransactionEntity> consumer) {
        if (userEntity == null) {
            return;
        }

        userEntity.getBankAccountEntities().stream()
                .flatMap(bankAccount -> bankAccount.getTransactionEntities().stream())
                .forEach(consumer);
    }

    /**
     * Создаёт список транзакций с использованием Supplier.
     *
     * @param supplier - поставщик
     * @return созданный список транзакций
     */
    public List<TransactionEntity> createTransactionList(Supplier<List<TransactionEntity>> supplier) {
        return supplier.get();
    }
}
