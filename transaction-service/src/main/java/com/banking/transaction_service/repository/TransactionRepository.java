package com.banking.transaction_service.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find by referenceId (UUID) — for idempotency checks
    Optional<Transaction> findByReferenceId(String referenceId);

    // All transactions for a given account (sent or received)
    List<Transaction> findBySenderAccountIdOrderByCreatedAtDesc(Long senderAccountId);

    List<Transaction> findByReceiverAccountIdOrderByCreatedAtDesc(Long receiverAccountId);

    // All transactions involving an account (either side)
    List<Transaction> findBySenderAccountIdOrReceiverAccountIdOrderByCreatedAtDesc(
            Long senderAccountId, Long receiverAccountId);
}