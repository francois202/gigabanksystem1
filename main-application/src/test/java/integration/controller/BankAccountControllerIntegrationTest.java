package integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.request.AuthRequest;
import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.request.TransactionGenerateRequest;
import gigabank.accountmanagement.dto.response.TransactionGenerateResponse;
import gigabank.accountmanagement.dto.response.UserAccountResponse;
import gigabank.accountmanagement.exception.ErrorResponse;
import gigabank.accountmanagement.model.UserEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.UserRepository;
import gigabank.accountmanagement.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.MvcNamespaceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.clearAllCaches;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GigaBankApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(ports = 9092)
@ActiveProfiles("test")
public class BankAccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    private static final Long userId = 1L;

    private static final Long accountId = 1L;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("accountsCache") != null) {
            Objects.requireNonNull(cacheManager.getCache("accountsCache")).clear();
        }
        if (cacheManager.getCache("usersCache") != null) {
            Objects.requireNonNull(cacheManager.getCache("usersCache")).clear();
        }
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void whenUserAccessAdminEndpoint_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    public void whenAdminAccessAdminEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    public void whenUserAccessGetAccountsEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/api/account-actions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    public void whenAdminAccessGetAccountsEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/api/account-actions"))
                .andExpect(status().isOk());
    }

    @Test
    void whenUnauthorizedUserGetAccountsEndpoint_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/account-actions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    void whenAdminAccessGetTransactionsEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    void whenUserAccessGetTransactionsEndpoint_thenOk() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void whenUnauthorizedUserAccessGetTransactionsEndpoint_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenTransferMoney_thenOk() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/transactions/generate?count=2"))
                .andExpect(status().isOk())
                .andReturn();

        TransactionGenerateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TransactionGenerateResponse.class
        );

        assertEquals("success", response.getStatus());
        assertEquals(2, response.getGenerated());
        assertNotNull(response.getTransactions());
        assertEquals(2, response.getTransactions().size());

        TransactionGenerateRequest firstTransaction = response.getTransactions().get(0);
        assertNotNull(firstTransaction.getTransactionId());
        assertNotNull(firstTransaction.getAmount());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenTransferNegativeAmount_thenBadRequest() throws Exception {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invalid_deposit_data.json");

        DepositWithdrawRequest request = objectMapper.readValue(inputStream, DepositWithdrawRequest.class);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/account-actions/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Сумма снятия превышает баланс на счете"));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void whenTransferMoneyToNonExistingAccount_thenNotFound() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invalid_deposit_data.json");
        DepositWithdrawRequest request = objectMapper.readValue(inputStream, DepositWithdrawRequest.class);

        String requestBody = objectMapper.writeValueAsString(request);
        long nonExistentAccountId = 999L;

        mockMvc.perform(post("/api/account-actions/" + nonExistentAccountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Аккаунт не найден с id: " + nonExistentAccountId));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void whenGetNonExistentAccount_thenNotFound() throws Exception {
        long nonExistentAccountId = 999L;

        mockMvc.perform(get("/api/account-actions/" + nonExistentAccountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Аккаунт не найден с id: " + nonExistentAccountId));
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void getUserAccounts_shouldCacheResult() throws Exception {
        mockMvc.perform(get("/api/account-actions/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Cache cache = cacheManager.getCache("accountsCache");
        assertNotNull(cache, "accountsCache should exist");

        Cache.ValueWrapper wrapper = cache.get("user_" + userId);
        assertNotNull(wrapper, "User accounts should be cached");

        bankAccountRepository.deleteAll();
        userRepository.deleteById(userId);

        mockMvc.perform(get("/api/account-actions/user/" + userId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void deposit_shouldEvictAccountCache() throws Exception {
        mockMvc.perform(get("/api/account-actions/user/" + userId))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("accountsCache");
        assertNotNull(cache);
        assertNotNull(cache.get("user_" + userId), "Account should be cached before deposit");

        DepositWithdrawRequest depositRequest = new DepositWithdrawRequest();
        depositRequest.setAmount(new BigDecimal("100.00"));

        String requestBody = objectMapper.writeValueAsString(depositRequest);
        mockMvc.perform(post("/api/account-actions/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));

        assertNull(cache.get("user_" + userId), "Account cache should be evicted after deposit");
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void withdraw_shouldEvictAccountCache() throws Exception {
        DepositWithdrawRequest depositRequest = new DepositWithdrawRequest();
        depositRequest.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/account-actions/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/account-actions/user/" + userId))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("accountsCache");
        assertNotNull(cache);
        assertNotNull(cache.get("user_" + userId), "Account should be cached before withdraw");

        DepositWithdrawRequest withdrawRequest = new DepositWithdrawRequest();
        withdrawRequest.setAmount(new BigDecimal("100.00"));

        String requestBody = objectMapper.writeValueAsString(withdrawRequest);

        mockMvc.perform(post("/api/account-actions/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        assertNull(cache.get("user_" + userId), "Account cache should be evicted after withdraw");
    }

    @Test
    @WithMockUser(username = "user", password = "password", roles = "USER")
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    void toggleAccountBlock_shouldEvictCache() throws Exception {
        mockMvc.perform(get("/api/account-actions/user/" + userId))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("accountsCache");
        assertNotNull(cache);
        assertNotNull(cache.get("user_" + userId), "Account should be cached before toggle");

        mockMvc.perform(post("/api/account-actions/" + accountId + "/block"))
                .andExpect(status().isOk());

        assertNull(cache.get("user_" + userId), "Account cache should be evicted after toggle block");
    }
}
