package com.banking.notification_service.service;

import org.springframework.stereotype.Service;

import com.banking.notification_service.dto.TransactionEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    public void processTransactionEvent(TransactionEvent event) {
        log.info("Processing notification for eventType={}, referenceId={}",
                event.getEventType(), event.getReferenceId());

        switch (event.getEventType()) {
            case "TRANSACTION_COMPLETED":
                handleTransactionCompleted(event);
                break;
            case "TRANSACTION_FAILED":
                handleTransactionFailed(event);
                break;
            case "TRANSACTION_REVERSED":
                handleTransactionReversed(event);
                break;
            default:
                log.warn("Unknown eventType={}, skipping notification", event.getEventType());
        }
    }

    private void handleTransactionCompleted(TransactionEvent event) {
        // In a real app, you'd fetch user email from auth-service by accountId
        // For now we simulate with a placeholder email
        String recipientEmail = getEmailForAccount(event.getSenderAccountId());

        String subject = buildCompletedSubject(event);
        String body = buildCompletedBody(event);

        emailService.sendEmail(recipientEmail, subject, body);

        // Also notify receiver for TRANSFER
        if ("TRANSFER".equals(event.getTransactionType())
                && event.getReceiverAccountId() != null) {
            String receiverEmail = getEmailForAccount(event.getReceiverAccountId());
            String receiverBody = buildReceiverBody(event);
            emailService.sendEmail(receiverEmail,
                    "Money received - " + event.getAmount() + " " + event.getCurrency(),
                    receiverBody);
        }
    }

    private void handleTransactionFailed(TransactionEvent event) {
        String recipientEmail = getEmailForAccount(event.getSenderAccountId());
        String subject = "Transaction failed - Ref: " + event.getReferenceId();
        String body = buildFailedBody(event);
        emailService.sendEmail(recipientEmail, subject, body);
    }

    private void handleTransactionReversed(TransactionEvent event) {
        String recipientEmail = getEmailForAccount(event.getSenderAccountId());
        String subject = "Transaction reversed - Ref: " + event.getReferenceId();
        String body = buildReversedBody(event);
        emailService.sendEmail(recipientEmail, subject, body);
    }

    // ---- Email body builders ----

    private String buildCompletedSubject(TransactionEvent event) {
        return switch (event.getTransactionType()) {
            case "TRANSFER" -> String.format("Transfer of %s %s completed successfully",
                    event.getAmount(), event.getCurrency());
            case "DEPOSIT" -> String.format("Deposit of %s %s received",
                    event.getAmount(), event.getCurrency());
            case "WITHDRAWAL" -> String.format("Withdrawal of %s %s processed",
                    event.getAmount(), event.getCurrency());
            default -> "Transaction completed - Ref: " + event.getReferenceId();
        };
    }

    private String buildCompletedBody(TransactionEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <div style="background: #1a73e8; padding: 20px; text-align: center;">
                    <h2 style="color: white; margin: 0;">Transaction Successful ✓</h2>
                  </div>
                  <div style="padding: 30px; background: #f9f9f9;">
                    <p style="font-size: 16px; color: #333;">Your transaction has been processed successfully.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Reference ID</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #333;">%s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Type</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #333;">%s</td>
                      </tr>
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Amount</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #1a73e8; font-size: 18px; font-weight: bold;">%s %s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Status</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">
                          <span style="background: #e6f4ea; color: #137333; padding: 4px 10px; border-radius: 12px; font-weight: bold;">COMPLETED</span>
                        </td>
                      </tr>
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Description</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #333;">%s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold; color: #555;">Timestamp</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #333;">%s</td>
                      </tr>
                    </table>
                    <p style="margin-top: 24px; font-size: 12px; color: #999;">
                      If you did not initiate this transaction, please contact support immediately.
                    </p>
                  </div>
                </body>
                </html>
                """
                .formatted(
                        event.getReferenceId(),
                        event.getTransactionType(),
                        event.getAmount(), event.getCurrency(),
                        event.getDescription() != null ? event.getDescription() : "N/A",
                        event.getTimestamp());
    }

    private String buildReceiverBody(TransactionEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <div style="background: #1a73e8; padding: 20px; text-align: center;">
                    <h2 style="color: white; margin: 0;">Money Received ✓</h2>
                  </div>
                  <div style="padding: 30px; background: #f9f9f9;">
                    <p style="font-size: 16px;">You have received a transfer.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Amount Received</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #1a73e8; font-size: 18px; font-weight: bold;">%s %s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Reference ID</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s</td>
                      </tr>
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Description</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s</td>
                      </tr>
                    </table>
                  </div>
                </body>
                </html>
                """
                .formatted(
                        event.getAmount(), event.getCurrency(),
                        event.getReferenceId(),
                        event.getDescription() != null ? event.getDescription() : "N/A");
    }

    private String buildFailedBody(TransactionEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <div style="background: #d93025; padding: 20px; text-align: center;">
                    <h2 style="color: white; margin: 0;">Transaction Failed ✗</h2>
                  </div>
                  <div style="padding: 30px; background: #f9f9f9;">
                    <p style="font-size: 16px; color: #333;">Unfortunately, your transaction could not be completed.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Reference ID</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Amount</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s %s</td>
                      </tr>
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Reason</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0; color: #d93025; font-weight: bold;">%s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Status</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">
                          <span style="background: #fce8e6; color: #d93025; padding: 4px 10px; border-radius: 12px; font-weight: bold;">FAILED</span>
                        </td>
                      </tr>
                    </table>
                    <p style="margin-top: 24px; color: #555;">
                      No money has been deducted from your account. Please try again or contact support.
                    </p>
                  </div>
                </body>
                </html>
                """
                .formatted(
                        event.getReferenceId(),
                        event.getAmount(), event.getCurrency(),
                        event.getFailureReason() != null ? event.getFailureReason() : "Unknown error");
    }

    private String buildReversedBody(TransactionEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                  <div style="background: #f9ab00; padding: 20px; text-align: center;">
                    <h2 style="color: white; margin: 0;">Transaction Reversed</h2>
                  </div>
                  <div style="padding: 30px; background: #f9f9f9;">
                    <p style="font-size: 16px; color: #333;">A transaction has been reversed on your account.</p>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                      <tr style="background: #fff;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Reference ID</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s</td>
                      </tr>
                      <tr style="background: #f5f5f5;">
                        <td style="padding: 12px; border: 1px solid #e0e0e0; font-weight: bold;">Reversed Amount</td>
                        <td style="padding: 12px; border: 1px solid #e0e0e0;">%s %s</td>
                      </tr>
                    </table>
                  </div>
                </body>
                </html>
                """.formatted(
                event.getReferenceId(),
                event.getAmount(), event.getCurrency());
    }

    // ---- Helper ----
    // In production: call auth-service to fetch real user email by accountId
    // For now: simulate with placeholder
    private String getEmailForAccount(Long accountId) {
        return "user_account_" + accountId + "@banking-demo.com";
    }
}
