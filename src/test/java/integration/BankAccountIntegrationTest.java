package integration;

import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.UserRepository;
import gigabank.accountmanagement.service.BankAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = GigaBankApplication.class)
@Transactional
public class BankAccountIntegrationTest {

    private BankAccountService bankAccountService;
    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;

    @Autowired
    public BankAccountIntegrationTest(BankAccountService bankAccountService, BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankAccountService = bankAccountService;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    private UserEntity createTestUser(String name, String email, String phone) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        return userRepository.save(user);
    }

    private BankAccountEntity createTestAccount(UserEntity user, String accountNumber, BigDecimal balance) {
        BankAccountEntity account = new BankAccountEntity();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setOwner(user);
        account.setBlocked(false);
        return bankAccountRepository.save(account);
    }

    private DepositWithdrawRequest createTestTransferRequest(BigDecimal amount) {
        DepositWithdrawRequest request = new DepositWithdrawRequest();
        request.setAmount(amount);
        request.setDescription("Transfer between accounts");
        return request;
    }

    @Test
    public void testCreateAndFindAccount() {
        UserEntity user = createTestUser("Test User", "test@example.com", "+1234567890");

        BankAccountEntity account = createTestAccount(user, "TEST001", new BigDecimal("1000.00"));

        BankAccountEntity foundAccount = bankAccountService.findAccountById(account.getId());

        assertNotNull(foundAccount);
        assertEquals(account.getId(), foundAccount.getId());
        assertEquals("TEST001", foundAccount.getAccountNumber());
        assertEquals(new BigDecimal("1000.00"), foundAccount.getBalance());
    }

    @Test
    public void testTransferBetweenAccounts() {
        UserEntity sender = createTestUser("Sender User", "sender@example.com", "+1111111111");
        UserEntity receiver = createTestUser("Receiver User", "receiver@example.com", "+2222222222");

        BankAccountEntity senderAccount = createTestAccount(sender, "SENDER001", new BigDecimal("2000.00"));
        BankAccountEntity receiverAccount = createTestAccount(receiver, "RECEIVER001", new BigDecimal("500.00"));

        BigDecimal transferAmount = new BigDecimal("300.00");

        DepositWithdrawRequest withdrawRequest = createTestTransferRequest(transferAmount);
        DepositWithdrawRequest depositRequest = createTestTransferRequest(transferAmount);

        bankAccountService.withdraw(senderAccount.getId(), withdrawRequest);
        bankAccountService.deposit(receiverAccount.getId(), depositRequest);

        BankAccountEntity updatedSender = bankAccountRepository.findById(senderAccount.getId()).orElseThrow();
        BankAccountEntity updatedReceiver = bankAccountRepository.findById(receiverAccount.getId()).orElseThrow();

        assertEquals(new BigDecimal("1700.00"), updatedSender.getBalance());
        assertEquals(new BigDecimal("800.00"), updatedReceiver.getBalance());
    }
}