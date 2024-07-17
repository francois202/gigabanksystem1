package gigabank.test.bankaccountservice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static gigabank.test.TestUtils.*;


public class RemoveBankAccountTest {

    @Test
    public void bankAccountMustBeRemoved() {
        boolean bankAccountIsExists = true;
        bankAccountService.removeBankAccount(userMaria, bankAccountTest1);
        bankAccountIsExists = userMaria.getBankAccounts().contains(bankAccountTest1);
        assertFalse(bankAccountIsExists);
    }
    @Test
    public void bankAccountMustNotRemovedWithUserNull() {
        boolean bankAccountIsExists = true;
        bankAccountIsExists = bankAccountService.removeBankAccount(userNull, bankAccountTest1);
        assertFalse(bankAccountIsExists);
    }

    @Test
    public void bankAccountMustNotRemovedWithBankAccountNull() {
        boolean bankAccountIsExists = true;
        bankAccountIsExists = bankAccountService.removeBankAccount(userIvan, bankAccountNull);
        assertFalse(bankAccountIsExists);
    }

    @Test
    public void bankAccountMustNotRemovedIfUserDontHaveThisBankAccount() {
        boolean userHaveRemovingBankAccount = true;
        userHaveRemovingBankAccount = bankAccountService.removeBankAccount(userIvan, bankAccountTest3);
        assertFalse(userHaveRemovingBankAccount);
    }
}