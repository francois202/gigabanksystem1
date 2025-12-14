package gigabank.accountmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gigabank.accountmanagement.dto.kafka.TransactionMessage;
import gigabank.accountmanagement.model.OutboxMessage;
import gigabank.accountmanagement.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, TransactionMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> messages = outboxMessageRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (messages.isEmpty()) {
            log.debug("Нет непрочитанных сообщений в outbox");
            return;
        }

        log.info("Найдено {} непрочитанных сообщений в outbox", messages.size());

        for (OutboxMessage message : messages) {
            try {
                log.info("Обрабатываем outbox сообщение ID: {}", message.getId());

                TransactionMessage transactionMessage = objectMapper.readValue(message.getPayload(), TransactionMessage.class);

                kafkaTemplate.send("transaction-outbox-events", message.getAggregateId(), transactionMessage);

                message.setProcessed(true);
                outboxMessageRepository.save(message);

                log.info("Outbox сообщение обработано: messageId={}", message.getId());
            }
            catch (Exception e) {
                log.error("Ошибка обработки сообщения {}: {}", message.getId(), e.getMessage());
            }
        }
    }
}
