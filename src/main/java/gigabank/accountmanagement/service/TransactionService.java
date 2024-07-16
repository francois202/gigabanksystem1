package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


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

    public List<Transaction> filterTransactions(User user, Predicate<Transaction> predicate) {
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public List<String> transformTransactions(User user, Function<Transaction, String> stringTransaction) {
        if (user == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .map(stringTransaction)
                .collect(Collectors.toList());
    }

    public void processTransactions(User user, Consumer<Transaction> consumer) {
        if (user == null) {
            return;
        }

        user.getBankAccounts().stream()
                .flatMap(bankAccount -> bankAccount.getTransactions().stream())
                .forEach(consumer);
    }

    public List<Transaction> createTransactionList(Supplier<List<Transaction>> supplier){
        return supplier.get();
    }
}
