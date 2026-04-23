package com.banking.transaction_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    // Event type — consumer uses this to decide what notification to send
    private String eventType; // TRANSACTION_COMPLETED, TRANSACTION_FAILED, TRANSACTION_REVERSED

    private Long transactionId;
    private String referenceId;
    private Long senderAccountId;
    private Long receiverAccountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL
    private String status;
    private String description;
    private String failureReason;
    private LocalDateTime timestamp;
}
