package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.CreateAccountRequest;
import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.entity.TransactionType;
import gigabank.accountmanagement.entity.User;
import gigabank.accountmanagement.service.notification.NotificationAdapter;
import gigabank.accountmanagement.service.notification.NotificationAdapterFactory;
import gigabank.accountmanagement.service.paymentstrategy.PaymentStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
public class BankAccountService {
    private final Map<User, List<BankAccount>> userBankAccounts;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationAdapter notificationAdapter;

    public BankAccountService(PaymentGatewayService paymentGatewayService, NotificationAdapterFactory factory) {
        this.paymentGatewayService = paymentGatewayService;
        this.notificationAdapter = factory.getNotificationAdapter();
        this.userBankAccounts = new HashMap<>();
    }

    public BankAccount createAccount(CreateAccountRequest request) {
        try {
            Integer userId = request.userId();
            User user = new User();
            user.setId(String.valueOf(userId));
            String accountNumber = "ACC_" + System.currentTimeMillis();
            BankAccount account = new BankAccount(accountNumber, new ArrayList<>());
            account.setOwner(user);
            account.setBalance(request.initialBalance());
            userBankAccounts.computeIfAbsent(account.getOwner(), k -> new ArrayList<>()).add(account);
            return account;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный формат userId");
        }
    }

    public BankAccount getAccount(String id) {
        return userBankAccounts.values().stream()
                .flatMap(List::stream)
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void deposit(String id, BigDecimal amount) {
        BankAccount account = getAccount(id);
        if (account == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Учетная запись не найдена",
                    new IllegalArgumentException("Неверный ID учетной записи")
            );
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(account.getBalance().add(amount));
            Transaction transaction = new Transaction(
                    "DEP_" + System.currentTimeMillis(),
                    amount,
                    TransactionType.DEPOSIT,
                    "Deposit",
                    account,
                    LocalDateTime.now(),
                    null, null, null, null, null
            );
            account.getTransactions().add(transaction);
        }
    }

    public void withdraw(String id, BigDecimal amount) {
        BankAccount account = getAccount(id);
        if (account != null && account.getBalance().compareTo(amount) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(account.getBalance().subtract(amount));
            Transaction transaction = new Transaction(
                    "WDR_" + System.currentTimeMillis(),
                    amount,
                    TransactionType.WITHDRAWAL,
                    "Withdrawal",
                    account,
                    LocalDateTime.now(),
                    null, null, null, null, null
            );
            account.getTransactions().add(transaction);
        }
    }

    public void transfer(String fromId, String toId, BigDecimal amount) {
        BankAccount fromAccount = getAccount(fromId);
        BankAccount toAccount = getAccount(toId);

        if (fromAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Счёт отправителя с ID " + fromId + " не найден");
        }
        if (toAccount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Счёт получателя с ID " + toId + " не найден");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недостаточно средств на счёте " + fromId);
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        Transaction fromTransaction = new Transaction(
                "TRF_" + System.currentTimeMillis() + "_FROM",
                amount,
                TransactionType.TRANSFER,
                "Transfer Out",
                fromAccount,
                LocalDateTime.now(),
                null, null, null, null, null
        );
        Transaction toTransaction = new Transaction(
                "TRF_" + System.currentTimeMillis() + "_TO",
                amount,
                TransactionType.TRANSFER,
                "Transfer In",
                toAccount,
                LocalDateTime.now(),
                null, null, null, null, null
        );
        fromAccount.getTransactions().add(fromTransaction);
        toAccount.getTransactions().add(toTransaction);
    }

    public List<Transaction> getTransactions(String id) {
        BankAccount account = getAccount(id);
        return account != null ? account.getTransactions() : null;
    }

    public void processPayment(BankAccount bankAccount, BigDecimal value, PaymentStrategy strategy, Map<String, String> details) {
        Objects.requireNonNull(bankAccount, "BankAccount must not be null");
        Objects.requireNonNull(strategy, "PaymentStrategy must not be null");
        Objects.requireNonNull(details, "Details map must not be null");

        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment value must be positive and non-null");
        }
        boolean success = paymentGatewayService.processPayment(value, details);
        if (!success) {
            throw new RuntimeException("Payment could not be processed: check the transaction details -" + details);
        }
        bankAccount.setBalance(bankAccount.getBalance().subtract(value));
        strategy.process(bankAccount, value, details);
        User user = bankAccount.getOwner();
        String message = String.format("Платеж на сумму %s успешно выполнен. Категория: %s. Платеж успешно обработан для счета: %s", value, details.getOrDefault("category", "Не указана"), bankAccount.getId());
        notificationAdapter.sendPaymentNotification(user, message);
        System.out.println(message);
    }
}