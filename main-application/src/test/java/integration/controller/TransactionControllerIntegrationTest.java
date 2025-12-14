package integration.controller;

import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import gigabank.accountmanagement.repository.UserRepository;
import gigabank.accountmanagement.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GigaBankApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(ports = 9092)
@ActiveProfiles("test")
public class TransactionControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static Long accountId;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("accountsCache") != null) {
            Objects.requireNonNull(cacheManager.getCache("accountsCache")).clear();
        }
        if (cacheManager.getCache("usersCache") != null) {
            Objects.requireNonNull(cacheManager.getCache("usersCache")).clear();
        }
        if (cacheManager.getCache("transactionsCache") != null) {
            Objects.requireNonNull(cacheManager.getCache("transactionsCache")).clear();
        }

        transactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        UserEntity user = new UserEntity();
        user.setEmail("test@email.com");
        user.setName("Test User");
        user.setPhoneNumber("+1234567890");
        UserEntity savedUser = userRepository.save(user);

        BankAccountEntity account = new BankAccountEntity();
        account.setAccountNumber("ACC001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setOwner(savedUser);
        account.setBlocked(false);
        BankAccountEntity savedAccount = bankAccountRepository.save(account);
        accountId = savedAccount.getId();

        for (int i = 0; i < 3; i++) {
            TransactionEntity transaction = TransactionEntity.builder()
                    .value(new BigDecimal("100.00"))
                    .type(TransactionType.DEPOSIT)
                    .category("Test Category " + i)
                    .createdDate(LocalDateTime.now())
                    .bankAccountEntity(savedAccount)
                    .build();
            transactionRepository.save(transaction);
        }
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void getAccountTransactions_shouldCacheResult() throws Exception {
        Cache cache = cacheManager.getCache("transactionsCache");
        Assertions.assertNotNull(cache);
        assertNull(cache.get("account_" + accountId), "Cache should be empty before first request");

        mockMvc.perform(get("/api/account-actions/" + accountId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        assertNotNull(cache, "transactionsCache should exist");
        Cache.ValueWrapper wrapper = cache.get("account_" + accountId);
        Assertions.assertNotNull(wrapper);
        assertNotNull(wrapper, "Transactions should be cached after first request");

        Object cachedObject = wrapper.get();
        Assertions.assertNotNull(cachedObject);
        assertNotNull(cachedObject);
        assertInstanceOf(List.class, cachedObject);

        transactionRepository.deleteByAccountId(accountId);

        assertEquals(0, transactionRepository.findByAccountId(accountId).size());

        mockMvc.perform(get("/api/account-actions/" + accountId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void generateTransactions_shouldEvictTransactionsCache() {
        List<TransactionResponse> initialTransactions = transactionService.getAccountTransactions(accountId);
        assertEquals(3, initialTransactions.size());

        Cache cache = cacheManager.getCache("transactionsCache");
        Assertions.assertNotNull(cache);
        assertNotNull(Objects.requireNonNull(cache.get("account_" + accountId)), "Transactions should be cached before generation");

        transactionService.generateTransactions(2, "at-least-once");

        assertNull(cache.get("account_" + accountId), "Transactions cache should be evicted after generation");

        List<TransactionResponse> updatedTransactions = transactionService.getAccountTransactions(accountId);
        assertNotNull(updatedTransactions);
    }
}
