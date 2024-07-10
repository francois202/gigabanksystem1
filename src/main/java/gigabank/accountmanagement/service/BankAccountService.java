package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static gigabank.accountmanagement.entity.TransactionType.TRANSFER;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
public class BankAccountService {
    private Map<User, List<BankAccount>> userAccounts;

    public Boolean createNewBankAccountForUser(User user, BankAccount bankAccount) {
        if (bankAccount != null && user != null) {
            user.getBankAccounts().add(bankAccount);
            return true;
        } else {
            return false;
        }
    }

    public Boolean deleteUserBankAccount(User user, BankAccount bankAccount) {
        if (user == null) {
            return false;
        }
        if (user.getBankAccounts().contains(bankAccount)) {
            user.getBankAccounts().remove(bankAccount);
            return true;
        } else {
            throw new IllegalArgumentException("Bank account does not exist");
        }
    }

    public Boolean replenishmentBankAccount(BankAccount bankAccount, BigDecimal sum, Transaction transaction) {

        if (bankAccount == null || transaction == null) {
            return false;
        }

        BigDecimal balance = bankAccount.getBalance();


        if (sum.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(sum);
            bankAccount.getTransactions().add(transaction);
            return true;
        } else {
            return false;
        }
    }

    public Boolean paymentFromBankAccount(BankAccount bankAccount, Transaction transaction) {
        List<Transaction> transactions = bankAccount.getTransactions();

        if (bankAccount == null || transactions == null) {
            return false;
        }

        if (bankAccount.getBalance().compareTo(transaction.getValue()) >= 0) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(transaction.getValue()));
            transactions.add(transaction);
            return true;
        } else {
            return false;
        }
    }

    public Boolean paymentFromAndToAccount(BankAccount fromAccount, BankAccount toAccount, BigDecimal sum) {

        if (fromAccount == null || toAccount == null || sum.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (fromAccount.getBalance().compareTo(sum) >= 0) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(sum));
            toAccount.setBalance(toAccount.getBalance().add(sum));

            Transaction transactionFrom = new Transaction("1", sum, TRANSFER, "Transfer", LocalDateTime.now());
            Transaction transactionTo = new Transaction("2", sum, TRANSFER, "Transfer", LocalDateTime.now());

            fromAccount.getTransactions().add(transactionFrom);
            toAccount.getTransactions().add(transactionTo);
            return true;
        } else {
            return false;
        }
    }
}
