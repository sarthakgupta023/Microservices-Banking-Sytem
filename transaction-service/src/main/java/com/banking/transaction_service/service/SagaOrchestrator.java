package com.banking.transaction_service.service;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.transaction_service.client.AccountServiceClient;
import com.banking.transaction_service.config.KafkaConfig;
import com.banking.transaction_service.dto.TransactionEvent;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    public Transaction executeTransferSaga(Transaction transaction) {
        log.info("SAGA START: referenceId={}, amount={}", transaction.getReferenceId(), transaction.getAmount());

        boolean debitSuccess = accountServiceClient.debit(
                transaction.getSenderAccountId(), transaction.getAmount(), transaction.getReferenceId());

        if (!debitSuccess) {
            return failTransaction(transaction, "Debit failed: insufficient funds or account error");
        }
        log.info("SAGA STEP 1 OK: Debited accountId={}", transaction.getSenderAccountId());

        boolean creditSuccess = accountServiceClient.credit(
                transaction.getReceiverAccountId(), transaction.getAmount(), transaction.getReferenceId());

        if (!creditSuccess) {
            log.warn("SAGA STEP 2 FAILED: initiating compensation...");
            boolean refundSuccess = accountServiceClient.refund(
                    transaction.getSenderAccountId(), transaction.getAmount(), transaction.getReferenceId());
            String reason = refundSuccess
                    ? "Credit failed. Sender refunded successfully."
                    : "Credit failed. WARNING: Refund also failed — manual intervention required!";
            return failTransaction(transaction, reason);
        }
        log.info("SAGA STEP 2 OK: Credited accountId={}", transaction.getReceiverAccountId());
        return completeTransaction(transaction);
    }

    public Transaction executeDepositSaga(Transaction transaction) {
        boolean creditSuccess = accountServiceClient.credit(
                transaction.getSenderAccountId(), transaction.getAmount(), transaction.getReferenceId());
        if (!creditSuccess)
            return failTransaction(transaction, "Deposit credit failed");
        return completeTransaction(transaction);
    }

    public Transaction executeWithdrawalSaga(Transaction transaction) {
        boolean debitSuccess = accountServiceClient.debit(
                transaction.getSenderAccountId(), transaction.getAmount(), transaction.getReferenceId());
        if (!debitSuccess)
            return failTransaction(transaction, "Withdrawal failed: insufficient funds");
        return completeTransaction(transaction);
    }

    private Transaction completeTransaction(Transaction transaction) {
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        publishEvent(saved, "TRANSACTION_COMPLETED");
        log.info("SAGA COMPLETE: referenceId={}", transaction.getReferenceId());
        return saved;
    }

    private Transaction failTransaction(Transaction transaction, String reason) {
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        transaction.setFailureReason(reason);
        transaction.setCompletedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        publishEvent(saved, "TRANSACTION_FAILED");
        log.error("SAGA FAILED: referenceId={}, reason={}", transaction.getReferenceId(), reason);
        return saved;
    }

    private void publishEvent(Transaction transaction, String eventType) {
        TransactionEvent event = TransactionEvent.builder()
                .eventType(eventType)
                .transactionId(transaction.getId())
                .referenceId(transaction.getReferenceId())
                .senderAccountId(transaction.getSenderAccountId())
                .receiverAccountId(transaction.getReceiverAccountId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .failureReason(transaction.getFailureReason())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(KafkaConfig.TRANSACTION_EVENTS_TOPIC, transaction.getReferenceId(), event);
        log.info("Kafka event published: eventType={}, referenceId={}", eventType, transaction.getReferenceId());
    }
}