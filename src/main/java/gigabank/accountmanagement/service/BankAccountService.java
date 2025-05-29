package gigabank.accountmanagement.service;

import gigabank.accountmanagement.entity.BankAccount;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.entity.Transaction;
import gigabank.accountmanagement.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Сервис отвечает за управление счетами, включая создание, удаление и пополнение
 */
@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @SneakyThrows
    @Transactional
    public void transfer(String fromAccount, String toAccount, BigDecimal amount)  {
        BankAccount source = accountRepository.findByAccountNumber(fromAccount)
                .orElseThrow(() -> new AccountNotFoundException(fromAccount));

        BankAccount target = accountRepository.findByAccountNumber(toAccount)
                .orElseThrow(() -> new AccountNotFoundException(toAccount));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException();
        }

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        Transaction debit = new Transaction();
        debit.setAmount(amount.negate());
        debit.setAccount(source);

        Transaction credit = new Transaction();
        credit.setAmount(amount);
        credit.setAccount(target);

        transactionRepository.saveAll(List.of(debit, credit));
    }
}
