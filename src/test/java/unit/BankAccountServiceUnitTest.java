package unit;

import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.exception.AccountNotFoundException;
import gigabank.accountmanagement.exception.OperationForbiddenException;
import gigabank.accountmanagement.exception.ValidationException;
import gigabank.accountmanagement.mapper.BankAccountMapper;
import gigabank.accountmanagement.mapper.TransactionMapper;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import gigabank.accountmanagement.service.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceUnitTest {
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountMapper bankAccountMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private BankAccountService bankAccountService;

    private BankAccountEntity testAccount;
    private BankAccountEntity testAccount2;

    private DepositWithdrawRequest withdrawRequest;
    private DepositWithdrawRequest depositRequest;

    private BankAccountResponse bankAccountResponse;

    @BeforeEach
    public void setUp() {
        testAccount = new BankAccountEntity();
        testAccount.setId(1L);
        testAccount.setAccountNumber("TEST123");
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setBlocked(false);

        testAccount2 = new BankAccountEntity();
        testAccount2.setId(2L);
        testAccount2.setAccountNumber("TEST456");
        testAccount2.setBalance(new BigDecimal("500.00"));
        testAccount2.setBlocked(false);

        withdrawRequest = new DepositWithdrawRequest();
        withdrawRequest.setAmount(new BigDecimal("300.00"));
        withdrawRequest.setDescription("Test withdrawal");

        depositRequest = new DepositWithdrawRequest();
        depositRequest.setAmount(new BigDecimal("500.00"));
        depositRequest.setDescription("Test deposit");

        bankAccountResponse = new BankAccountResponse();
        bankAccountResponse.setId(1L);
        bankAccountResponse.setAccountNumber("TEST123");
        bankAccountResponse.setBalance(new BigDecimal("500.00"));
        bankAccountResponse.setBlocked(false);
    }

    @Test
    public void testGetAllAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<BankAccountEntity> accounts = Arrays.asList(testAccount, testAccount2);
        Page<BankAccountEntity> accountsPage = new PageImpl<>(accounts, pageable, accounts.size());

        when(bankAccountRepository.findAll(pageable)).thenReturn(accountsPage);

        Page<BankAccountResponse> result = bankAccountService.getAllAccounts(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(bankAccountRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testFindAccountById_success() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        BankAccountEntity result = bankAccountService.findAccountById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST123", result.getAccountNumber());
        verify(bankAccountRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindAccountById_notFound_fail() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.findAccountById(999L));

        verify(bankAccountRepository, times(1)).findById(999L);
    }

    @Test
    public void testCloseAccount_success() {
        testAccount.setBalance(BigDecimal.ZERO);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        doNothing().when(transactionRepository).deleteByAccountId(1L);
        doNothing().when(bankAccountRepository).deleteById(1L);

        bankAccountService.closeAccount(1L);

        verify(transactionRepository, times(1)).deleteByAccountId(1L);
        verify(transactionRepository, times(1)).deleteByAccountId(1L);
        verify(bankAccountRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testCloseAccount_withNonZeroBalance_fail() {
        testAccount.setBalance(new BigDecimal("100.00"));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(ValidationException.class,
                () -> bankAccountService.closeAccount(1L));

        verify(bankAccountRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).deleteByAccountId(anyLong());
        verify(bankAccountRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testCloseAccount_accountNotFound_fail() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.closeAccount(999L));

        verify(bankAccountRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).deleteByAccountId(anyLong());
        verify(bankAccountRepository, never()).deleteById(anyLong());
    }

//    @Test
//    public void testToggleAccountBlock_blockAccount_success() {
//        testAccount.setBlocked(false);
//        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
//        when(bankAccountRepository.save(testAccount)).thenReturn(testAccount);
//
//        bankAccountResponse.setBlocked(true);
//
//        BankAccountResponse result = bankAccountService.toggleAccountBlock(1L);
//
//        assertTrue(result.isBlocked());
//        verify(bankAccountRepository, times(1)).findById(1L);
//        verify(bankAccountRepository, times(1)).save(testAccount);
//    }

//    @Test
//    public void testToggleAccountBlock_unblockAccount_success() {
//        testAccount.setBlocked(true);
//        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
//        when(bankAccountRepository.save(testAccount)).thenReturn(testAccount);
//
//        bankAccountResponse.setBlocked(false);
//
//        BankAccountResponse result = bankAccountService.toggleAccountBlock(1L);
//
//        assertFalse(result.isBlocked());
//        verify(bankAccountRepository, times(1)).findById(1L);
//        verify(bankAccountRepository, times(1)).save(testAccount);
//        verify(bankAccountMapper, times(1)).toResponse(testAccount);
//    }

    @Test
    public void testToggleAccountBlock_accountNotFound_fail() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.toggleAccountBlock(999L));
        verify(bankAccountRepository, times(1)).findById(999L);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testDeposit_success() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.save(testAccount)).thenReturn(testAccount);

        bankAccountService.deposit(1L, depositRequest);

        assertEquals(new BigDecimal("1500.00"), testAccount.getBalance());
        verify(bankAccountRepository, times(1)).findById(1L);
        verify(bankAccountRepository, times(1)).save(testAccount);
    }

    @Test
    public void testDeposit_notFound_fail() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.deposit(999L, depositRequest));

        verify(bankAccountRepository, times(1)).findById(999L);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testDeposit_blockedAccount_fail() {
        testAccount.setBlocked(true);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(OperationForbiddenException.class,
                () -> bankAccountService.deposit(1L, depositRequest));

        verify(bankAccountRepository, times(1)).findById(1L);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdraw_success() {
        testAccount.setBalance(new BigDecimal("1000.00"));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.save(testAccount)).thenReturn(testAccount);

        bankAccountService.withdraw(1L, withdrawRequest);

        assertEquals(new BigDecimal("700.00"), testAccount.getBalance());
        verify(bankAccountRepository, times(1)).findById(1L);
        verify(bankAccountRepository, times(1)).save(testAccount);
    }

    @Test
    public void testWithdraw_blockedAccount_fail() {
        testAccount.setBlocked(true);
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(OperationForbiddenException.class,
                () -> bankAccountService.withdraw(1L, withdrawRequest));

        verify(bankAccountRepository, times(1)).findById(1L);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdraw_validation_fail() {
        testAccount.setBlocked(false);
        testAccount.setBalance(new BigDecimal("100.00"));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(ValidationException.class,
                () -> bankAccountService.withdraw(1L, withdrawRequest));

        verify(bankAccountRepository, times(1)).findById(1L);
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdraw_accountNotFound_fail() {
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> bankAccountService.withdraw(999L, withdrawRequest));

        verify(bankAccountRepository, times(1)).findById(999L);
        verify(bankAccountRepository, never()).save(any());
    }
}
