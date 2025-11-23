package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private boolean isBlocked;
}
