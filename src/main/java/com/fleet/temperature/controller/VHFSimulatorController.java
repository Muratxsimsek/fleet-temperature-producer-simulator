package com.fleet.temperature.controller;

import com.fleet.temperature.dto.VHFMessageDto;
import com.fleet.temperature.service.VHFSimulatorService;
import com.fleet.temperature.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/vhf-simulator")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VHFSimulatorController {
    
    private final VHFSimulatorService simulatorService;
    private final KafkaProducerService producerService;
    
    @PostMapping("/start")
    public ResponseEntity<String> startSimulation(@RequestParam("mode") String mode) {
        try {
            VHFSimulatorService.SimulationMode simulationMode = VHFSimulatorService.SimulationMode.valueOf(mode.toUpperCase());
            simulatorService.startSimulation(simulationMode);
            return ResponseEntity.ok("Simulation started in " + simulationMode.getDescription() + " mode");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid mode. Available modes: LOW, MEDIUM, HIGH, VARIABLE");
        } catch (Exception e) {
            log.error("Error starting simulation", e);
            return ResponseEntity.internalServerError().body("Error starting simulation: " + e.getMessage());
        }
    }
    
    @PostMapping("/stop")
    public ResponseEntity<String> stopSimulation() {
        try {
            simulatorService.stopSimulation();
            return ResponseEntity.ok("Simulation stopped");
        } catch (Exception e) {
            log.error("Error stopping simulation", e);
            return ResponseEntity.internalServerError().body("Error stopping simulation: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<VHFSimulatorService.SimulationStatus> getSimulationStatus() {
        try {
            VHFSimulatorService.SimulationStatus status = simulatorService.getSimulationStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting simulation status", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/send-message")
    public ResponseEntity<String> sendSingleMessage() {
        try {
            // Generate a single message using the message generator
            VHFMessageDto message = simulatorService.generateSingleMessage();
            producerService.sendVHFMessage(message);
            return ResponseEntity.ok("Single message sent");
        } catch (Exception e) {
            log.error("Error sending single message", e);
            return ResponseEntity.internalServerError().body("Error sending message: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-batch")
    public ResponseEntity<String> sendBatchMessages(@RequestParam("count") int count) {
        try {
            if (count <= 0 || count > 10000) {
                return ResponseEntity.badRequest().body("Count must be between 1 and 10000");
            }
            
            List<VHFMessageDto> messages = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                messages.add(simulatorService.generateRealisticMessage());
            }
            
            producerService.sendVHFMessages(messages);
            return ResponseEntity.ok("Batch of " + count + " messages sent");
        } catch (Exception e) {
            log.error("Error sending batch messages", e);
            return ResponseEntity.internalServerError().body("Error sending batch messages: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<KafkaProducerService.MessageStats> getProducerStats() {
        try {
            KafkaProducerService.MessageStats stats = producerService.getMessageStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting producer stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/reset-stats")
    public ResponseEntity<String> resetStats() {
        try {
            producerService.resetStats();
            return ResponseEntity.ok("Statistics reset");
        } catch (Exception e) {
            log.error("Error resetting stats", e);
            return ResponseEntity.internalServerError().body("Error resetting stats: " + e.getMessage());
        }
    }
    
    @GetMapping("/modes")
    public ResponseEntity<VHFSimulatorService.SimulationMode[]> getAvailableModes() {
        try {
            VHFSimulatorService.SimulationMode[] modes = VHFSimulatorService.SimulationMode.values();
            return ResponseEntity.ok(modes);
        } catch (Exception e) {
            log.error("Error getting available modes", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("VHF Simulator is running");
    }
}
