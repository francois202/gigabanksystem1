package gigabank.accountmanagement.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {


    public static Set<String> transactionCategories = Set.of(
            "Food", "Health", "Beauty", "Education");

    public Set<String> validateCategories (Set<String> categories) {
        Set<String> result = new HashSet<>();
        for (String category : categories) {
            if (transactionCategories.contains(category))
                result.add(category);
        }
        return result;
    }
}
