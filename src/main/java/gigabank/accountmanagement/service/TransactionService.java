package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;

import javax.swing.text.html.ListView;
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
        return category != null && transactionCategories.contains(category);

    }

    public Set<String> validateCategories(Set<String> category) {
        Set<String> validCategories = new HashSet<>();

        for (String cat : category) {
            if (isValidCategory(cat)) {
                validCategories.add(cat);
            }
        }
        return validCategories;
    }
}
