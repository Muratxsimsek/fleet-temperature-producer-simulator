package com.fleet.temperature.service;

import com.fleet.temperature.dto.VHFMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class VHFSimulatorService {
    
    private final VHFMessageGenerator messageGenerator;
    private final KafkaProducerService kafkaProducerService;
    private final AircraftDataGenerator aircraftDataGenerator;
    
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(0);
    
    private final Map<String, Double> aircraftTemperatures = new ConcurrentHashMap<>();
    
    public enum SimulationMode {
        LOW(100, "100 mesaj/saniye"),
        MEDIUM(1000, "1000 mesaj/saniye"),
        HIGH(5000, "5000 mesaj/saniye"),
        VARIABLE(0, "Değişken hız (100-5000 mesaj/saniye)");
        
        private final int messagesPerSecond;
        private final String description;
        
        SimulationMode(int messagesPerSecond, String description) {
            this.messagesPerSecond = messagesPerSecond;
            this.description = description;
        }
        
        public int getMessagesPerSecond() {
            return messagesPerSecond;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private SimulationMode currentMode = SimulationMode.MEDIUM;
    
    public void startSimulation(SimulationMode mode) {
        if (isRunning.compareAndSet(false, true)) {
            currentMode = mode;
            startTime.set(System.currentTimeMillis());
            totalMessagesSent.set(0);
            initializeAircraftTemperatures();
            
            log.info("Starting VHF simulation in {} mode", mode.getDescription());
            startSimulationTask();
        } else {
            log.warn("Simulation is already running");
        }
    }
    
    public void stopSimulation() {
        if (isRunning.compareAndSet(true, false)) {
            log.info("Stopping VHF simulation. Total messages sent: {}", totalMessagesSent.get());
        } else {
            log.warn("Simulation is not running");
        }
    }
    
    public SimulationStatus getSimulationStatus() {
        return SimulationStatus.builder()
                .isRunning(isRunning.get())
                .currentMode(currentMode)
                .totalMessagesSent(totalMessagesSent.get())
                .durationMs(System.currentTimeMillis() - startTime.get())
                .messagesPerSecond(calculateCurrentMessagesPerSecond())
                .build();
    }
    
    private void startSimulationTask() {
        if (currentMode == SimulationMode.VARIABLE) {
            startVariableSpeedSimulation();
        } else {
            startFixedSpeedSimulation();
        }
    }
    
    private void startFixedSpeedSimulation() {
        new Thread(() -> {
            while (isRunning.get()) {
                try {
                    int messagesToSend = currentMode.getMessagesPerSecond();
                    List<VHFMessageDto> messages = new ArrayList<>();
                    
                    for (int i = 0; i < messagesToSend && isRunning.get(); i++) {
                        messages.add(generateRealisticMessage());
                    }
                    
                    if (!messages.isEmpty()) {
                        kafkaProducerService.sendVHFMessages(messages);
                        totalMessagesSent.addAndGet(messages.size());
                    }
                    
                    Thread.sleep(1000); // 1 saniye bekle
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in simulation task", e);
                }
            }
        }, "VHF-Simulator-Thread").start();
    }
    
    private void startVariableSpeedSimulation() {
        new Thread(() -> {
            while (isRunning.get()) {
                try {
                    // Rastgele hız değişimi: 100-5000 mesaj/saniye
                    int messagesToSend = 100 + (int) (Math.random() * 4900);
                    List<VHFMessageDto> messages = new ArrayList<>();
                    
                    for (int i = 0; i < messagesToSend && isRunning.get(); i++) {
                        messages.add(generateRealisticMessage());
                    }
                    
                    if (!messages.isEmpty()) {
                        kafkaProducerService.sendVHFMessages(messages);
                        totalMessagesSent.addAndGet(messages.size());
                    }
                    
                    Thread.sleep(1000); // 1 saniye bekle
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error in variable speed simulation task", e);
                }
            }
        }, "VHF-Variable-Simulator-Thread").start();
    }
    
    public VHFMessageDto generateRealisticMessage() {
        String aircraftId = aircraftDataGenerator.getRandomAircraftId();
        double currentTemp = aircraftTemperatures.getOrDefault(aircraftId, 22.0);
        
        // Sıcaklık trendi oluştur
        double newTemp = aircraftDataGenerator.generateTemperatureTrend(aircraftId, currentTemp);
        aircraftTemperatures.put(aircraftId, newTemp);
        
        return messageGenerator.generateVHFMessageForAircraft(aircraftId);
    }
    
    private void initializeAircraftTemperatures() {
        aircraftTemperatures.clear();
        for (int i = 0; i < aircraftDataGenerator.getAircraftCount(); i++) {
            String aircraftId = aircraftDataGenerator.getRandomAircraftId();
            aircraftTemperatures.put(aircraftId, aircraftDataGenerator.generateRealisticTemperature());
        }
    }
    
    public void sendTrendBasedMessage(String aircraftId) {
        double currentTemp = aircraftTemperatures.getOrDefault(aircraftId, 22.0);
        double newTemp = aircraftDataGenerator.generateTemperatureTrend(aircraftId, currentTemp);
        aircraftTemperatures.put(aircraftId, newTemp);
        
        VHFMessageDto message = messageGenerator.generateTrendBasedMessage(aircraftId, newTemp);
        kafkaProducerService.sendVHFMessage(message);
        totalMessagesSent.incrementAndGet();
    }
    
    public VHFMessageDto generateSingleMessage() {
        return messageGenerator.generateVHFMessage();
    }
    
    private long calculateCurrentMessagesPerSecond() {
        long duration = System.currentTimeMillis() - startTime.get();
        if (duration > 0) {
            return (totalMessagesSent.get() * 1000) / duration;
        }
        return 0;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SimulationStatus {
        private boolean isRunning;
        private SimulationMode currentMode;
        private long totalMessagesSent;
        private long durationMs;
        private long messagesPerSecond;
    }
}
