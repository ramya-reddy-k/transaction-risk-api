package com.ramya.transactionrisk.outbox;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.kafka.enabled",
        havingValue = "true"
)
public class KafkaOutboxPublisher {

    private final TransactionOutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final int maximumAttempts;
    private final Duration sendTimeout;
    private final Duration staleProcessingTimeout;

    public KafkaOutboxPublisher(
            TransactionOutboxService outboxService,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic,
            @Value("${app.kafka.maximum-attempts:5}") int maximumAttempts,
            @Value("${app.kafka.send-timeout:PT10S}") Duration sendTimeout,
            @Value("${app.kafka.stale-processing-timeout:PT5M}")
            Duration staleProcessingTimeout
    ) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.maximumAttempts = maximumAttempts;
        this.sendTimeout = sendTimeout;
        this.staleProcessingTimeout = staleProcessingTimeout;
    }

    @Scheduled(
            fixedDelayString =
                    "${app.kafka.publisher-delay-ms:5000}"
    )
    public void publishPendingEvents() {
        for (OutboxMessage message :
                outboxService.claimBatch(
                        maximumAttempts,
                        staleProcessingTimeout
                )) {
            publish(message);
        }
    }

    private void publish(OutboxMessage message) {
        try {
            kafkaTemplate.send(
                            topic,
                            message.aggregateId(),
                            message.payload()
                    )
                    .get(
                            sendTimeout.toMillis(),
                            TimeUnit.MILLISECONDS
                    );

            outboxService.markPublished(message.id());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            outboxService.markFailed(
                    message.id(),
                    "Kafka publication interrupted"
            );
        } catch (Exception exception) {
            outboxService.markFailed(
                    message.id(),
                    rootMessage(exception)
            );
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? current.getClass().getSimpleName()
                : current.getMessage();
    }
}
