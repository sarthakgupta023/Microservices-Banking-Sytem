package com.banking.account_service.dto;

import java.math.BigDecimal;

import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.validator.constraints.NotBlank;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class TransactionRequest {

    @NotBlank(message = "Account number required")
    private String accountNumber;

    @NotNull(message = "Amount required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
