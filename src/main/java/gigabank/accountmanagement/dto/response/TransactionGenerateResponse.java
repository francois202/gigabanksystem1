package gigabank.accountmanagement.dto.response;

import gigabank.accountmanagement.dto.request.TransactionGenerateRequest;
import lombok.Data;

import java.util.List;

@Data
public class TransactionGenerateResponse {
    private String status;
    private String mode;
    private Integer generated;
    private List<TransactionGenerateRequest> transactions;
}
