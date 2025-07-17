package org.example;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


public class TransactionService {

    public static final Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education");

    public static Boolean isValidTransactionCategory(String category) {
        return TRANSACTION_CATEGORIES.contains(category);
    }

    public Set<String> validateTransactionCategories(Set <String> categories) {
     Set<String> validCategories = new HashSet<>();

     for (String category : categories) {
         if (isValidTransactionCategory(category)) {
             validCategories.add(category);
         }
     }
     return validCategories;
    }
}
