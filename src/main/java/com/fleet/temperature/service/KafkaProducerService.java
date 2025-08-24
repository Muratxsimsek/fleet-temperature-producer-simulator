package com.fleet.temperature.service;

import com.fleet.temperature.dto.VHFMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    
    private final KafkaTemplate<String, VHFMessageDto> kafkaTemplate;
    
    @Value("${kafka.topic.fleet-cabin-temperature-raw}")
    private String topic;
    
    private final AtomicLong messageCounter = new AtomicLong(0);
    private final AtomicLong successCounter = new AtomicLong(0);
    private final AtomicLong errorCounter = new AtomicLong(0);
    
    public CompletableFuture<SendResult<String, VHFMessageDto>> sendVHFMessage(VHFMessageDto message) {
        messageCounter.incrementAndGet();
        
        // Use aircraftId as partition key for better ordering within the same aircraft
        String partitionKey = message.getAircraftId();
        CompletableFuture<SendResult<String, VHFMessageDto>> future = kafkaTemplate.send(topic, partitionKey, message);
        
        future.whenComplete((result, throwable) -> {
            if (throwable == null) {
                successCounter.incrementAndGet();
                log.debug("Message sent successfully: {} to topic: {} with partition key: {}", 
                         message.getMessageId(), topic, partitionKey);
            } else {
                errorCounter.incrementAndGet();
                log.error("Failed to send message: {} to topic: {} with partition key: {}", 
                         message.getMessageId(), topic, partitionKey, throwable);
            }
        });
        
        return future;
    }
    
    public void sendVHFMessages(List<VHFMessageDto> messages) {
        log.info("Sending batch of {} messages to Kafka topic: {} with partition key strategy", messages.size(), topic);
        
        for (VHFMessageDto message : messages) {
            sendVHFMessage(message);
        }
    }
    
    public MessageStats getMessageStats() {
        return MessageStats.builder()
                .totalMessages(messageCounter.get())
                .successfulMessages(successCounter.get())
                .failedMessages(errorCounter.get())
                .successRate(calculateSuccessRate())
                .build();
    }
    
    public void resetStats() {
        messageCounter.set(0);
        successCounter.set(0);
        errorCounter.set(0);
        log.info("Message statistics reset");
    }
    
    private double calculateSuccessRate() {
        long total = messageCounter.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successCounter.get() / total * 100.0;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MessageStats {
        private long totalMessages;
        private long successfulMessages;
        private long failedMessages;
        private double successRate;
    }
}
