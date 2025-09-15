package gigabank.accountmanagement.service;

import gigabank.accountmanagement.exception.AccountNotFoundException;
import gigabank.accountmanagement.exception.OperationForbiddenException;
import gigabank.accountmanagement.exception.ValidationException;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import gigabank.accountmanagement.repository.UserRepository;
import gigabank.accountmanagement.service.notification.NotificationService;
import gigabank.accountmanagement.service.payment.PaymentGatewayService;
import gigabank.accountmanagement.service.payment.strategies.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
@Transactional
public class BankAccountService {
    private static final Logger logger = Logger.getLogger(BankAccountService.class.getName());

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;

    @Qualifier("emailNotificationService")
    private final NotificationService notificationService;

    @Autowired
    public BankAccountService(BankAccountRepository bankAccountRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              @Qualifier("emailNotificationService") NotificationService notificationService,
                              PaymentGatewayService paymentGatewayService) {
        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.notificationService = notificationService;
        logger.info("Initializing test accounts data...");
    }

    @PostConstruct
    public void init() {
        logger.info("PostConstruct method called - BankAccountService bean initialization complete");
    }

    @PreDestroy
    public void cleanup() {
        logger.info("PreDestroy method called - BankAccountService bean is about to be destroyed");
    }

    public Page<BankAccountEntity> getAllAccounts(Pageable pageable) {
        return bankAccountRepository.findAll(pageable);
    }

    public BankAccountEntity findAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public void closeAccount(Long accountId) {
        BankAccountEntity account = findAccountById(accountId);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Невозможно закрыть счет с ненулевым балансом");
        }
        transactionRepository.deleteByAccountId(accountId);
        bankAccountRepository.deleteById(accountId);
    }

    public BankAccountEntity toggleAccountBlock(Long accountId) {
        BankAccountEntity account = findAccountById(accountId);

        account.setBlocked(!account.isBlocked());

        return bankAccountRepository.save(account);
    }

    public void deposit(Long accountId, BigDecimal amount) {
        BankAccountEntity account = findAccountById(accountId);
        if (account.isBlocked()) {
            throw new OperationForbiddenException("Аккаунт заблокирован");
        }
        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);
    }

    public void withdraw(Long accountId, BigDecimal amount) {
        BankAccountEntity account = findAccountById(accountId);
        if (account.isBlocked()) {
            throw new OperationForbiddenException("Аккаунт заблокирован");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ValidationException("Сумма снятия превышает баланс на счете");
        }
        account.setBalance(account.getBalance().subtract(amount));
        bankAccountRepository.save(account);
    }

    public static BankAccountEntity createTestAccount() {
        UserEntity testUserEntity = new UserEntity();
        testUserEntity.setId(123L);
        testUserEntity.setName("John");
        testUserEntity.setEmail("john.doe@example.com");
        testUserEntity.setPhoneNumber("+1234567890");

        BankAccountEntity account = new BankAccountEntity();
        account.setId(124L);
        account.setBalance(new BigDecimal("5000.00"));
        account.setOwner(testUserEntity);

        TransactionEntity transactionEntity1 = TransactionEntity.builder()
                .id(125L)
                .value(new BigDecimal("100.00"))
                .type(TransactionType.PAYMENT)
                .category("Electronics")
                .createdDate(LocalDateTime.now().minusDays(5))
                .build();

        TransactionEntity transactionEntity2 = TransactionEntity.builder()
                .id(126L)
                .value(new BigDecimal("200.00"))
                .type(TransactionType.DEPOSIT)
                .category("Groceries")
                .createdDate(LocalDateTime.now().minusDays(2))
                .build();

        account.getTransactionEntities().addAll(List.of(transactionEntity1, transactionEntity2));

        return account;
    }
}
