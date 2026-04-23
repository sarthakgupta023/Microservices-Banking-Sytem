package com.banking.transaction_service.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import com.banking.transaction_service.dto.TransactionRequest;
import com.banking.transaction_service.dto.TransactionResponse;
import com.banking.transaction_service.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SagaOrchestrator sagaOrchestrator;

    public TransactionResponse initiateTransaction(TransactionRequest request) {

        // Step 1: Generate unique reference ID for this transaction
        String referenceId = UUID.randomUUID().toString();

        // Step 2: Save as PENDING immediately
        // This gives us a record even if something crashes mid-saga
        Transaction transaction = Transaction.builder()
                .referenceId(referenceId)
                .senderAccountId(request.getSenderAccountId())
                .receiverAccountId(request.getReceiverAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .type(Transaction.TransactionType.valueOf(request.getType().toUpperCase()))
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        Transaction pendingTxn = transactionRepository.save(transaction);
        log.info("Transaction created as PENDING: referenceId={}", referenceId);

        // Step 3: Execute correct Saga based on type
        Transaction result;
        switch (pendingTxn.getType()) {
            case TRANSFER:
                result = sagaOrchestrator.executeTransferSaga(pendingTxn);
                break;
            case DEPOSIT:
                result = sagaOrchestrator.executeDepositSaga(pendingTxn);
                break;
            case WITHDRAWAL:
                result = sagaOrchestrator.executeWithdrawalSaga(pendingTxn);
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + request.getType());
        }

        return mapToResponse(result);
    }

    public TransactionResponse getTransaction(Long id) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return mapToResponse(txn);
    }

    public TransactionResponse getTransactionByReference(String referenceId) {
        Transaction txn = transactionRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + referenceId));
        return mapToResponse(txn);
    }

    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        return transactionRepository
                .findBySenderAccountIdOrReceiverAccountIdOrderByCreatedAtDesc(
                        accountId, accountId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---- Mapper ----
    private TransactionResponse mapToResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .referenceId(txn.getReferenceId())
                .senderAccountId(txn.getSenderAccountId())
                .receiverAccountId(txn.getReceiverAccountId())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .type(txn.getType().name())
                .status(txn.getStatus().name())
                .description(txn.getDescription())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .build();
    }
}
