package com.banking.account_service.dto;

import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.validator.constraints.NotBlank;

import com.banking.account_service.entity.Account;

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