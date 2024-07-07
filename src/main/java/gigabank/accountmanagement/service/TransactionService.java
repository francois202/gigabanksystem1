package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;

import java.util.HashSet;
import java.util.Set;

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


}
