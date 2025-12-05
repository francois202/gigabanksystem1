package gigabank.accountmanagement.dto.kafka;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeadLetterMessage {
    private Object originalMessage;
    private String exceptionMessage;
    private String stackTrace;
    private LocalDateTime timestamp;
    private String topic;
    private Integer partition;
    private Long offset;
    private String consumerGroup;

    public DeadLetterMessage(Object originalMessage, Exception exception,
                             String topic, Integer partition, Long offset, String consumerGroup) {
        this.originalMessage = originalMessage;
        this.exceptionMessage = exception.getMessage();
        this.stackTrace = getStackTraceAsString(exception);
        this.timestamp = LocalDateTime.now();
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.consumerGroup = consumerGroup;
    }

    private String getStackTraceAsString(Exception exception) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
