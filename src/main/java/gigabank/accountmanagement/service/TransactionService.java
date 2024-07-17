package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    //почему здесь Set, а не List? Потому что категории должны быть уникальны.
    public static Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education", "Deposit", "Transfer");

    private static final boolean IS_SUCCESS = true;

    public boolean paymentTransaction(BankAccount bankAccount, Transaction transaction) {
        if (bankAccount == null || transaction == null
                || bankAccount.getBalance().compareTo(transaction.getValue()) < 0
                || transaction.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            return !IS_SUCCESS;
        }
        bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getValue()));
        transaction.setType(TransactionType.PAYMENT);
        bankAccount.getTransactions().add(transaction);
        transaction.setBankAccount(bankAccount);
        return IS_SUCCESS;
    }

    public boolean transferTransaction(BankAccount fromAccount, BankAccount toAccount, BigDecimal amount) {
        if (fromAccount == null || toAccount == null || amount == null
                || fromAccount.getBalance().compareTo(amount) < 0
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return !IS_SUCCESS;
        }

        String categoryTransfer = TRANSACTION_CATEGORIES.stream()
                .filter(c -> c.equals("Transfer"))
                .findFirst()
                .orElse("");

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        Transaction transactionFromAccount = new Transaction(
                amount,
                TransactionType.TRANSFER,
                categoryTransfer,
                fromAccount);

        fromAccount.getTransactions().add(transactionFromAccount);
        transactionFromAccount.setBankAccount(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
        Transaction transactionToAccount = new Transaction(
                amount,
                TransactionType.TRANSFER,
                categoryTransfer,
                toAccount);

        toAccount.getTransactions().add(transactionToAccount);
        transactionToAccount.setBankAccount(toAccount);
        return IS_SUCCESS;
    }

    //----Функциональные интерфейсы-----
    public List<Transaction> filterTransactions(User user, Predicate<Transaction> predicate) {
        List<Transaction> filteredTransactions = new ArrayList<>();
        if (user == null || predicate == null) {
            return filteredTransactions;
        }

        filteredTransactions = user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .filter(predicate)
                .collect(Collectors.toList());

        return filteredTransactions;
    }

    public List<String> transformTransactions(User user, Function<Transaction, String> function) {
        if (user == null || function == null) {
            return Collections.emptyList();
        }

        return user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .map(function)
                .collect(Collectors.toList());
    }

    public void processTransactions(User user, Consumer<Transaction> consumer) {
        if (user == null || consumer == null) {
            return;
        }

        user.getBankAccounts().stream()
                .flatMap(ba -> ba.getTransactions().stream())
                .forEach(consumer);
    }

    public List<Transaction> createTransactionList(Supplier<List<Transaction>> supplier) {
        if (supplier == null) {
            return Collections.emptyList();
        }
        return supplier.get();
    }

    public List<Transaction> mergeTransactionLists(
            List<Transaction> list1, List<Transaction> list2,
            BiFunction<List<Transaction>, List<Transaction>, List<Transaction>> biFunction) {

        if (list1 == null || list2 == null || biFunction == null) {
            return Collections.emptyList();
        }

        return biFunction.apply(list1, list2);
    }
}