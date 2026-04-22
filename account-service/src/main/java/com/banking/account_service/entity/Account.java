package com.banking.account_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique account number — auto generate karenge
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    // Auth service ka user ID — foreign key nahi, just reference
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_holder_name", nullable = false)
    private String accountHolderName;

    @Column(name = "email", nullable = false)
    private String email;

    // SAVINGS ya CURRENT
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    // Balance — BigDecimal use karo money ke liye (double nahi!)
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    // ACTIVE, INACTIVE, BLOCKED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AccountType {
        SAVINGS, CURRENT
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, BLOCKED
    }
}
