package org.example.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    public static final Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education");


    public Boolean isValidCategory(String category) {
        return TRANSACTION_CATEGORIES.contains(category);
    }

    public Set<String> validateCategories (Set<String> categories) {
        Set<String> result = new HashSet<>();
        for (String category : categories) {
            if (TRANSACTION_CATEGORIES.contains(category))
                result.add(category);
        }
        return result;
    }
}
