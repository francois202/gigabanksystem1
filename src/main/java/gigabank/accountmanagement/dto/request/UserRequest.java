package gigabank.accountmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private int accountId;
    private BigDecimal amount;
    private String paymentType;
    private Map<String, String> paymentDetails;
}
