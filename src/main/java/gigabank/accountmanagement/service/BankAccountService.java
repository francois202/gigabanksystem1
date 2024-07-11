package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private static final boolean IS_SUCCESS = true;
    private Map<User, List<BankAccount>> userAccounts = new HashMap<>();
    public Map<User, List<BankAccount>> getUserAccounts() {
        return this.userAccounts;
    }
    public boolean addNewBankAccount(User user) {
        if (user == null) {
            return !IS_SUCCESS;
        }
        BankAccount bankAccount = new BankAccount(user);
        user.getBankAccounts().add(bankAccount);
        userAccounts.computeIfAbsent(user, k -> user.getBankAccounts());
        return IS_SUCCESS;
    }

    public boolean removeBankAccount (User user, BankAccount bankAccount) {
        if (user == null || bankAccount == null ||
                !user.getBankAccounts().contains(bankAccount)) {
            return !IS_SUCCESS;
        }
        user.getBankAccounts().remove(bankAccount);
        return IS_SUCCESS;
    }

    public boolean depositToBankAccount (BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return !IS_SUCCESS;
        }

        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        Transaction transaction = new Transaction(
                amount,
                TransactionType.DEPOSIT,
                "Deposit",
                bankAccount);

        bankAccount.getTransactions().add(transaction);
        return IS_SUCCESS;
    }
}