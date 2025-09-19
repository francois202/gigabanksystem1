package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.response.BankAccountResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.exception.AccountNotFoundException;
import gigabank.accountmanagement.exception.OperationForbiddenException;
import gigabank.accountmanagement.exception.ValidationException;
import gigabank.accountmanagement.mapper.BankAccountMapper;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountMapper bankAccountMapper;

    /**
     * Получает все счета с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница с банковскими счетами в формате DTO
     */
    public Page<BankAccountResponse> getAllAccounts(Pageable pageable) {
        log.info("Попытка найти все счета.");
        Page<BankAccountEntity> accounts = bankAccountRepository.findAll(pageable);

        List<BankAccountResponse> responses = accounts.getContent().stream()
                .map(bankAccountMapper::toResponse)
                .toList();

        log.info("Найдено {} счетов", accounts.getTotalElements());
        return new PageImpl<>(responses, pageable, accounts.getTotalElements());
    }

    /**
     * Находит счет по идентификатору.
     *
     * @param accountId идентификатор счета
     * @return сущность банковского счета
     * @throws AccountNotFoundException если счет не найден
     */
    public BankAccountEntity findAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Счет с ID {} не найден", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    /**
     * Получает информацию о счете по его идентификатору.
     *
     * @param accountId идентификатор счета
     * @return DTO с информацией о счете
     * @throws AccountNotFoundException если счет не найден
     */
    public BankAccountResponse getAccountById(Long accountId) {
        log.info("Попытка поиска счета по id: {}", accountId);
        BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        log.info("Счет найден. id: {}", accountId);
        return bankAccountMapper.toResponse(account);
    }

    /**
     * Закрывает банковский счет.
     *
     * @param accountId идентификатор счета для закрытия
     * @throws AccountNotFoundException если счет не найден
     * @throws ValidationException если баланс счета не равен нулю
     */
    @Transactional
    public void closeAccount(Long accountId) {
        log.info("Попытка закрытия счета ID: {}", accountId);
        BankAccountEntity account = findAccountById(accountId);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Невозможно закрыть счет с ненулевым балансом");
        }
        transactionRepository.deleteByAccountId(accountId);
        bankAccountRepository.deleteById(accountId);
        log.info("Счет ID {} успешно закрыт", accountId);
    }

    /**
     * Переключает статус блокировки счета.
     *
     * @param accountId идентификатор счета
     * @return DTO обновленной сущности банковского счета
     * @throws AccountNotFoundException если счет не найден
     */
    @Transactional
    public BankAccountResponse toggleAccountBlock(Long accountId) {
        log.info("Переключение блокировки счета ID: {}", accountId);
        BankAccountEntity account = findAccountById(accountId);

        account.setBlocked(!account.isBlocked());

        return bankAccountMapper.toResponse(bankAccountRepository.save(account));
    }

    /**
     * Пополняет баланс счета на указанную сумму.
     *
     * @param accountId идентификатор счета
     * @param request DTO с данными для пополнения счета
     * @return DTO с обновленной информацией о счете
     * @throws AccountNotFoundException если счет не найден
     * @throws OperationForbiddenException если счет заблокирован
     */
    @Transactional
    public BankAccountResponse deposit(Long accountId, DepositWithdrawRequest request) {
        log.info("Пополнение счета ID: {} на сумму: {}", accountId, request.getAmount());

        BankAccountEntity account = findAccountById(accountId);
        if (account.isBlocked()) {
            throw new OperationForbiddenException("Аккаунт заблокирован");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        BankAccountResponse response = bankAccountMapper.toResponse(bankAccountRepository.save(account));

        log.info("Счет ID {} пополнен. Новый баланс: {}",
                accountId, account.getBalance());
        return response;
    }

    /**
     * Снимает указанную сумму с баланса счета.
     *
     * @param accountId идентификатор счета
     * @param request DTO с данными для снятия средств
     * @return DTO с обновленной информацией о счете
     * @throws AccountNotFoundException если счет не найден
     * @throws OperationForbiddenException если счет заблокирован
     * @throws ValidationException если сумма снятия превышает баланс
     */
    @Transactional
    public BankAccountResponse withdraw(Long accountId, DepositWithdrawRequest request) {
        log.info("Снятие со счета ID: {} суммы: {}", accountId, request.getAmount());
        BankAccountEntity account = findAccountById(accountId);
        if (account.isBlocked()) {
            throw new OperationForbiddenException("Аккаунт заблокирован");
        }
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new ValidationException("Сумма снятия превышает баланс на счете");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        BankAccountResponse response = bankAccountMapper.toResponse(bankAccountRepository.save(account));

        log.info("Со счета ID {} снято {}. Новый баланс: {}",
                accountId, request.getAmount(), account.getBalance());
        return response;
    }

    /**
     * Создает тестовый банковский счет.
     *
     * @return DTO созданного тестового счета
     */
    @Transactional
    public BankAccountResponse createTestAccount() {
        log.debug("Создание тестового счета");
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

        return bankAccountMapper.toResponse(account);
    }
}