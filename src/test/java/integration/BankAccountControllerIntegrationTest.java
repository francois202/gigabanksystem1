package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.request.AuthRequest;
import gigabank.accountmanagement.dto.request.DepositWithdrawRequest;
import gigabank.accountmanagement.dto.request.TransactionGenerateRequest;
import gigabank.accountmanagement.dto.response.TransactionGenerateResponse;
import gigabank.accountmanagement.exception.ErrorResponse;
import gigabank.accountmanagement.model.UserGenerated;
import gigabank.accountmanagement.service.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = GigaBankApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EmbeddedKafka(ports = 9092)
@ActiveProfiles("test")
public class BankAccountControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private String baseUrl;

    @Autowired
    private AuthService authService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final TestRestTemplate userRestTemplate = new TestRestTemplate("user", "password");

    private static final TestRestTemplate adminRestTemplate = new TestRestTemplate("admin", "admin");

    private static final TestRestTemplate noAuthRestTemplate = new TestRestTemplate();

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    private String getJwtToken(String username, String password) {
        AuthRequest authRequest = new AuthRequest(username, password);
        return authService.authenticate(authRequest).getToken();
    }

    @Test
    public void whenUserAccessAdminEndpoint_thenForbidden() {
        ResponseEntity<String> response = userRestTemplate.getForEntity(baseUrl + "/admin/users",
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void whenAdminAccessAdminEndpoint_thenOk() {
        ResponseEntity<String> response = adminRestTemplate.getForEntity(baseUrl + "/admin/users",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenUserAccessGetAccountsEndpoint_thenOk() {
        ResponseEntity<String> response = userRestTemplate.getForEntity(baseUrl + "/account-actions",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenAdminAccessGetAccountsEndpoint_thenOk() {
        ResponseEntity<String> response = adminRestTemplate.getForEntity(baseUrl + "/account-actions",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenUnauthorizedUserGetAccountsEndpoint_thenUnauthorized() {
        ResponseEntity<String> response = noAuthRestTemplate.getForEntity(baseUrl + "/account-actions",
                String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({
            "user, password",
            "admin, admin"
    })
    public void whenJwtTokenAccessGetAccountsEndpoint_thenOk(String username, String password) {
        String jwtToken = getJwtToken(username, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = noAuthRestTemplate.exchange(
                baseUrl + "/account-actions",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenInvalidJwtTokenAccessGetAccountsEndpoint_thenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = noAuthRestTemplate.exchange(
                baseUrl + "/account-actions",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void whenAdminAccessGetTransactionsEndpoint_thenOk() {
        ResponseEntity<String> response = adminRestTemplate.getForEntity(baseUrl + "/transactions",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenUserAccessGetTransactionsEndpoint_thenOk() {
        ResponseEntity<String> response = userRestTemplate.getForEntity(baseUrl + "/transactions",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void whenUnauthorizedUserAccessGetTransactionsEndpoint_thenUnauthorized() {
        ResponseEntity<String> response = noAuthRestTemplate.getForEntity(baseUrl + "/transactions",
                String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenTransferMoney_thenOk() {
        ResponseEntity<TransactionGenerateResponse> response = userRestTemplate.exchange(
                baseUrl + "/transactions/generate?count=2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        TransactionGenerateResponse responseBody = response.getBody();
        assertEquals("success", responseBody.getStatus());
        assertEquals(2, responseBody.getGenerated());
        assertNotNull(responseBody.getTransactions());
        assertEquals(2, responseBody.getTransactions().size());

        TransactionGenerateRequest firstTransaction = responseBody.getTransactions().get(0);
        assertNotNull(firstTransaction.getTransactionId());
        assertNotNull(firstTransaction.getAmount());
    }

    //использую выгрузку невалидных тестовых данных из джсона
    @Test
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenTransferNegativeAmount_thenBadRequest() throws IOException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invalid_deposit_data.json");

        DepositWithdrawRequest request = objectMapper.readValue(inputStream, DepositWithdrawRequest.class);

        long accountId = 1L;

        ResponseEntity<ErrorResponse> response = userRestTemplate.postForEntity(
                baseUrl + "/account-actions/" + accountId + "/withdraw",
                request, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Сумма снятия превышает баланс на счете", errorResponse.getMessage());
    }

    //использую выгрузку невалидных тестовых данных из джсона
    @Test
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenTransferMoneyToNonExistingAccount_thenNotFound() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invalid_deposit_data.json");

        DepositWithdrawRequest request = objectMapper.readValue(inputStream, DepositWithdrawRequest.class);

        long accountId = new Random().nextLong(3, 1000);

        ResponseEntity<ErrorResponse> response = userRestTemplate.postForEntity(
                baseUrl + "/account-actions/" + accountId + "/deposit",
                request, ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Аккаунт не найден с id: " + accountId, errorResponse.getMessage());
    }

    @Test
    @Sql(scripts = "/sql/insert-test-accounts.sql")
    public void whenGetNonExistentAccount_thenNotFound() {
        long accountId = new Random().nextLong(3, 1000);

        ResponseEntity<ErrorResponse> response = userRestTemplate.getForEntity(
                baseUrl + "/account-actions/" + accountId,
                ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Аккаунт не найден с id: " + accountId, errorResponse.getMessage());
    }

    //использую выгрузку тестовых данных из джсона и считываю их в сгенерированный pojo класс
    @Test
    public void whenGeneratedUserAccessGetTransactionsEndpoint_thenOk() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("user.json");

        UserGenerated userGenerated = objectMapper.readValue(inputStream, UserGenerated.class);

        TestRestTemplate generatedUserRestTemplate = new TestRestTemplate(userGenerated.getName(), userGenerated.getPassword());
        ResponseEntity<String> response = generatedUserRestTemplate.getForEntity(baseUrl + "/account-actions",
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
