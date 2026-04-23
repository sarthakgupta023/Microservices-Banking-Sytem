package com.banking.transaction_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Table;
import org.hibernate.type.EnumType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique ID for this transaction — used for idempotency and event tracking
    @Column(unique = true, nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private Long senderAccountId;

    // Null for DEPOSIT type
    private Long receiverAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    // Human-readable description e.g. "Rent payment"
    private String description;

    // Populated if status = FAILED — explains what went wrong
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ---- Enums ----

    public enum TransactionType {
        TRANSFER,    // sender → receiver
        DEPOSIT,     // external money coming in
        WITHDRAWAL   // money going out
    }

    public enum TransactionStatus {
        PENDING,     // Saga started, not yet complete
        COMPLETED,   // All steps succeeded
        FAILED,      // One step failed, compensation done
        REVERSED     // Manually reversed after completion
    }
}
