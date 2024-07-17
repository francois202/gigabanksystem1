package gigabank.test.bankaccountservice;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AddNewBankAccountTest {
    @BeforeAll
    static void Initializer() {
        usersInitializer();
        bankAccountsInitializer();
        transactionsInitializer();
    }
    @AfterEach
    void resetBankAccountBalance() {
        bankAccountTest1.setBalance(BigDecimal.ZERO);
    }

    @Test
    public void newBankAccountMustBeAdded() {
        boolean accountAdded = false;
        accountAdded = bankAccountService.addNewBankAccount(userIvan);
        String lastBankAccountId = userIvan.getBankAccounts()
                .get(userIvan.getBankAccounts().size() - 1).getId();

        //Проверяем, что новый счёт добавился в мапу
        boolean userAccountsMapContainsNewBankAccount = false;
        for (Map.Entry<User, List<BankAccount>> entry : bankAccountService.getUserAccounts().entrySet()) {
            for (BankAccount bankAccount : entry.getValue()) {
                if (bankAccount.getId().contains(lastBankAccountId)) {
                    userAccountsMapContainsNewBankAccount = true;
                    break;
                }
            }
        }
        assertTrue(accountAdded);
        assertTrue(userAccountsMapContainsNewBankAccount);
    }

    @Test
    public void oneUserMustHaveMultipleBankAccounts() {
        userIvan.getBankAccounts().clear();

        bankAccountService.addNewBankAccount(userIvan);
        bankAccountService.addNewBankAccount(userIvan);
        bankAccountService.addNewBankAccount(userIvan);

        int countUsers = 0;
        int countBankAccounts = 0;
        for (Map.Entry<User, List<BankAccount>> entry : bankAccountService.getUserAccounts().entrySet()) {
            if (entry.getKey().getFirstName().contains("Ivan")) {
                countUsers++;
            }
        }
        System.out.println();
        for (Map.Entry<User, List<BankAccount>> entry : bankAccountService.getUserAccounts().entrySet()) {
            for (BankAccount bankAccount : entry.getValue()) {
                if (bankAccount.getOwner().getFirstName().contains("Ivan")) {
                    countBankAccounts++;
                }
            }
        }
        assertEquals(1, countUsers);
        assertEquals(3, countBankAccounts);
    }

    @Test
    public void newBankAccountMustNotAddedWithUserNull() {
        boolean accountAdded = true;
        accountAdded = bankAccountService.addNewBankAccount(userNull);
        assertFalse(accountAdded);
    }
}