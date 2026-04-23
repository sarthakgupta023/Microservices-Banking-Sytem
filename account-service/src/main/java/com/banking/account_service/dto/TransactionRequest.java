package com.banking.account_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotBlank(message = "Account number required")
    private String accountNumber;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
