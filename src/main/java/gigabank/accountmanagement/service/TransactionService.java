package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Сервис отвечает за управление платежами и переводами
 */
public class TransactionService {

    //почему здесь Set, а не List? Потому что категории должны быть уникальны.
    public static Set<String> TRANSACTION_CATEGORIES = Set.of(
            "Health", "Beauty", "Education");

    private static final boolean IS_SUCCESS = true;

    public boolean paymentTransaction (BankAccount bankAccount, Transaction transaction) {
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

    public boolean transferTransaction (BankAccount fromAccount, BankAccount toAccount, BigDecimal amount) {
        if (fromAccount == null || toAccount == null || amount == null
                || fromAccount.getBalance().compareTo(amount) < 0
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return !IS_SUCCESS;
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        Transaction transactionFromAccount = new Transaction(
                amount,
                TransactionType.TRANSFER,
                "Transfer",
                fromAccount);

        fromAccount.getTransactions().add(transactionFromAccount);
        transactionFromAccount.setBankAccount(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
            Transaction transactionToAccount = new Transaction(
                    amount,
                    TransactionType.TRANSFER,
                    "Transfer",
                    toAccount);

            toAccount.getTransactions().add(transactionToAccount);
            transactionToAccount.setBankAccount(toAccount);
        return IS_SUCCESS;
    }
}