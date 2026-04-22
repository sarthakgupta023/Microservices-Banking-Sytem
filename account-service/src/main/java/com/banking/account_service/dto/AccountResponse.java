package com.banking.account_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banking.account_service.entity.Account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    
    private Long userId;
    private String accountHolderName;
    private String email;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private Account.AccountStatus status;
    private LocalDateTime createdAt;

    // Entity → Response convert karo
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountHolderName(account.getAccountHolderName())
                .email(account.getEmail())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}