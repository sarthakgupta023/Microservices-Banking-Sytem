package com.banking.account_service.dto;

import com.banking.account_service.entity.Account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull(message = "User ID required")
    private Long userId;

    @NotBlank(message = "Account holder name required")
    private String accountHolderName;

    @NotBlank(message = "Email required")
    private String email;

    @NotNull(message = "Account type required")
    private Account.AccountType accountType;
}