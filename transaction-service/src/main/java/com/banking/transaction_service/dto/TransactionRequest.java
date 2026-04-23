package com.banking.transaction_service.dto;

import java.math.BigDecimal;

import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotNull(message = "Sender account ID is required")
    private Long senderAccountId;

    // Optional for DEPOSIT type
    private Long receiverAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotNull
    private String type; // TRANSFER, DEPOSIT, WITHDRAWAL

    private String description;
}
