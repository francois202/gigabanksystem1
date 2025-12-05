package integration;

import gigabank.accountmanagement.GigaBankApplication;
import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.model.BankAccountEntity;
import gigabank.accountmanagement.model.OutboxMessage;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.repository.BankAccountRepository;
import gigabank.accountmanagement.repository.OutboxMessageRepository;
import gigabank.accountmanagement.repository.TransactionRepository;
import gigabank.accountmanagement.service.OutboxProcessor;
import gigabank.accountmanagement.service.TransactionService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = GigaBankApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EmbeddedKafka(ports = 9092)
@Sql(scripts = "/sql/insert-test-accounts.sql")
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    private OutboxProcessor outboxProcessor;

    private TransactionMessage testTransactionMessage;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        outboxMessageRepository.deleteAll();

        testTransactionMessage = TransactionMessage.builder()
                .id(100L)
                .bankAccountId(1L)
                .value(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .createdDate(LocalDateTime.now())
                .sourceAccount("SOURCE_ACC")
                .targetAccount("ACC001")
                .category("SALARY")
                .build();
    }

    @Test
    void transactionAndOutboxSavedAtomically() {
        transactionService.processTransaction(testTransactionMessage, "at-least-once");

        List<TransactionEntity> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);

        TransactionEntity savedTransaction = transactions.get(0);
        assertThat(savedTransaction.getValue()).isEqualTo(new BigDecimal("100.00"));
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(savedTransaction.getBankAccountEntity().getId()).isEqualTo(1L);

        List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
        assertThat(outboxMessages).hasSize(1);

        OutboxMessage outboxMessage = outboxMessages.get(0);
        assertThat(outboxMessage.getAggregateType()).isEqualTo("Transaction");
        assertThat(outboxMessage.getAggregateId()).isEqualTo("100");
        assertThat(outboxMessage.getEventType()).isEqualTo("MoneyTransferCompleted");
        assertThat(outboxMessage.getPayload()).contains("\"transactionId\"");
        assertThat(outboxMessage.isProcessed()).isFalse();

        BankAccountEntity updatedAccount = bankAccountRepository.findById(1L).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualTo(new BigDecimal("1000100.00"));
    }

    @Test
    @Transactional
    void shouldRollbackTransactionWhenErrorOccurs() {
        TransactionMessage invalidTransaction = TransactionMessage.builder()
                .id(200L)
                .bankAccountId(999L)
                .value(new BigDecimal("100.00"))
                .type(TransactionType.DEPOSIT)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.processTransaction(invalidTransaction, "at-least-once");
        });

        assertThat(transactionRepository.findAll()).isEmpty();
        assertThat(outboxMessageRepository.findAll()).isEmpty();

        BankAccountEntity account1 = bankAccountRepository.findById(1L).orElseThrow();
        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("1000000.00"));
    }

    @Test
    void outboxProcessorShouldHandleEmptyOutboxGracefully() {
        outboxProcessor.processOutboxMessages();
        assertThat(outboxMessageRepository.findByProcessedFalseOrderByCreatedAtAsc()).isEmpty();
    }

    @Test
    @Transactional
    void outboxProcessorShouldSendMessagesToKafkaAndMarkAsProcessed() {
        OutboxMessage unprocessedMessage = new OutboxMessage();
        unprocessedMessage.setAggregateType("Transaction");
        unprocessedMessage.setAggregateId("300");
        unprocessedMessage.setEventType("MoneyTransferCompleted");
        unprocessedMessage.setPayload("{\"transactionId\": 300, \"amount\": 500.00, \"status\": \"COMPLETED\"}");
        unprocessedMessage.setProcessed(false);
        outboxMessageRepository.save(unprocessedMessage);

        outboxProcessor.processOutboxMessages();

        OutboxMessage processedMessage = outboxMessageRepository.findById(unprocessedMessage.getId()).orElseThrow();
        assertThat(processedMessage.isProcessed()).isTrue();

        try (Consumer<String, String> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(Collections.singletonList("transaction-outbox-events"));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
            assertThat(records).isNotEmpty();

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo("300");
            assertThat(record.value()).contains("\"transactionId\": 300");
            assertThat(record.value()).contains("\"amount\": 500.00");
        }
    }
}