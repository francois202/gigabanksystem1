package gigabank.accountmanagement.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
@Service
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