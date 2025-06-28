package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

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
public class TransactionService {
    public static final Set<String> transactionCategories = Set.of(
            "Health", "Beauty", "Education");

    public Boolean isValidCategory(String category) {
        return category != null && transactionCategories.contains(category);
    }

    public synchronized Set<String> validateCategories(Set<String> categories) {
        return categories.stream()
                .filter(this::isValidCategory)
                .collect(Collectors.toSet());
    }
}
