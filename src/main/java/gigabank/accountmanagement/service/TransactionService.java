package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

import java.io.File;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

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

    public List<Transaction> filterTransactions(User user, Predicate<Transaction> predicate) {
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<String> transformTransactions(User user, Function<Transaction, String> function) {
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .map(function)
                .collect(Collectors.toList());
    }

    public void processTransactions(User user, Consumer<Transaction> consumer) {
        if (user == null) {
            return; // Завершаем выполнение метода
        }

        user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .forEach(consumer);
    }

    public List<Transaction> createTransactionList(Supplier<List<Transaction>> supplier) {
        return supplier.get();
    }

    public List<Transaction> mergeTransactionLists(List<Transaction> transaction1, List<Transaction> transaction2,
                                                   BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> biFunction) {

        if (transaction1 == null || transaction2 == null) {
            return Collections.emptyList();
        }


        return biFunction.apply(transaction1, transaction2);
    }


}
