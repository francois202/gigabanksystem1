package gigabank.test;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.User;
import org.junit.jupiter.api.Test;

import static gigabank.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class AddNewBankAccountTest {

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

        int userMapKeySize = 0;
        int userMapValueSize = 0;
        for (Map.Entry<User, List<BankAccount>> entry : bankAccountService.getUserAccounts().entrySet()) {
            if (entry.getKey().getFirstName().contains("Ivan")) {
                userMapKeySize++;
            }
        }
        for (Map.Entry<User, List<BankAccount>> entry : bankAccountService.getUserAccounts().entrySet()) {
            for (BankAccount bankAccount : entry.getValue()) {
                if (bankAccount.getOwner().getFirstName().contains("Ivan")) {
                    userMapValueSize++;
                }
            }
        }
        assertEquals(1, userMapKeySize);
        assertEquals(3, userMapValueSize);
    }

    @Test
    public void newBankAccountMustNotAddedWithUserNull() {
        boolean accountAdded = true;
        accountAdded = bankAccountService.addNewBankAccount(userNull);
        assertFalse(accountAdded);
    }
}
