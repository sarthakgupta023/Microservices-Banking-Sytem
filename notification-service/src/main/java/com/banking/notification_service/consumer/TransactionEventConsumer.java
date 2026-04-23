package com.banking.notification_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.banking.notification_service.dto.TransactionEvent;
import com.banking.notification_service.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "transaction-events", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionEvent(
            @Payload TransactionEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Kafka message received: topic={}, partition={}, offset={}, eventType={}, referenceId={}",
                topic, partition, offset, event.getEventType(), event.getReferenceId());

        try {
            notificationService.processTransactionEvent(event);
        } catch (Exception e) {
            // Log the error — KafkaConsumerConfig's error handler will retry
            // After max retries, the message goes to a dead-letter topic (if configured)
            log.error("Failed to process event: referenceId={}, error={}",
                    event.getReferenceId(), e.getMessage(), e);
            // Re-throw so the error handler retries
            throw e;
        }
    }
}
