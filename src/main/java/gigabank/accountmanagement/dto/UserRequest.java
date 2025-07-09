package gigabank.accountmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class UserRequest {
    private int accountId;
    private BigDecimal amount;
    private String paymentType;
    private Map<String, String> paymentDetails;
}