package gigabank.accountmanagement.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    //почему здесь Set, а не List?
    public static Set<String> transactionCategories = Set.of(
            "Health", "Beauty", "Education");

    public Boolean isValidCategory(String category) {
        return transactionCategories.contains(category);
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

}
