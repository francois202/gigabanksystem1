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
}
