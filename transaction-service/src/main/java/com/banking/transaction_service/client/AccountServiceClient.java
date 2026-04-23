package com.banking.transaction_service.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(
            @Value("${services.account-service.url}") String accountServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(accountServiceUrl)
                .build();
    }

    // Debit (subtract) amount from an account
    // Returns true if successful, false if insufficient funds or account not found
    public boolean debit(Long accountId, BigDecimal amount, String referenceId) {
        try {
            webClient.post()
                    .uri("/api/accounts/{id}/debit", accountId)
                    .bodyValue(Map.of(
                            "amount", amount,
                            "referenceId", referenceId))
                    .retrieve()
                    .toBodilessEntity()
                    .block(); // synchronous — wait for response
            log.info("Debit successful: accountId={}, amount={}", accountId, amount);
            return true;
        } catch (WebClientResponseException e) {
            log.error("Debit failed: accountId={}, status={}, body={}",
                    accountId, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Debit error: accountId={}, error={}", accountId, e.getMessage());
            return false;
        }
    }

    // Credit (add) amount to an account
    public boolean credit(Long accountId, BigDecimal amount, String referenceId) {
        try {
            webClient.post()
                    .uri("/api/accounts/{id}/credit", accountId)
                    .bodyValue(Map.of(
                            "amount", amount,
                            "referenceId", referenceId))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Credit successful: accountId={}, amount={}", accountId, amount);
            return true;
        } catch (WebClientResponseException e) {
            log.error("Credit failed: accountId={}, status={}", accountId, e.getStatusCode());
            return false;
        } catch (Exception e) {
            log.error("Credit error: accountId={}, error={}", accountId, e.getMessage());
            return false;
        }
    }

    // Compensating action — refund sender if credit step failed
    public boolean refund(Long accountId, BigDecimal amount, String referenceId) {
        log.warn("SAGA COMPENSATION: Refunding accountId={}, amount={}", accountId, amount);
        // Refund = credit the sender back
        return credit(accountId, amount, "REFUND-" + referenceId);
    }
}
