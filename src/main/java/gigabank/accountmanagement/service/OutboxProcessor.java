package gigabank.accountmanagement.service;

import gigabank.accountmanagement.model.OutboxMessage;
import gigabank.accountmanagement.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> messages = outboxMessageRepository
                .findByProcessedFalseOrderByCreatedAtAsc();

        if (!messages.isEmpty()) {
            log.debug("Нет непрочитанных сообщений в outbox");
        }

        for (OutboxMessage message : messages) {
            try {
                kafkaTemplate.send("transaction-outbox-events", message.getAggregateId(), message.getPayload());

                log.debug("Сообщение отправлено в Kafka: messageId={}, aggregateId={}",
                        message.getId(), message.getAggregateId());

                message.setProcessed(true);
                outboxMessageRepository.save(message);

                log.info("Outbox сообщение обработано: messageId={}, eventType={}",
                        message.getId(), message.getEventType());
            }
            catch (Exception e) {
                log.error("Ошибка обработки сообщения {}: {}", message.getId(), e.getMessage());
            }
        }
    }
}
