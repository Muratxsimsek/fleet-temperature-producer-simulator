package com.fleet.temperature.service;

import com.fleet.temperature.dto.VHFMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VHFMessageGenerator {
    
    private final AircraftDataGenerator aircraftDataGenerator;
    
    public VHFMessageDto generateVHFMessage() {
        return VHFMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .aircraftId(aircraftDataGenerator.getRandomAircraftId())
                .sensorType("CABIN_TEMP_V1")
                .schemaVersion("1.0")
                .ts(Instant.now())
                .payload(VHFMessageDto.TemperaturePayloadDto.builder()
                        .cabinZone(aircraftDataGenerator.getRandomCabinZone())
                        .temperatureC(aircraftDataGenerator.generateRealisticTemperature())
                        .build())
                .build();
    }
    
    public VHFMessageDto generateVHFMessageForAircraft(String aircraftId) {
        return VHFMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .aircraftId(aircraftId)
                .sensorType("CABIN_TEMP_V1")
                .schemaVersion("1.0")
                .ts(Instant.now())
                .payload(VHFMessageDto.TemperaturePayloadDto.builder()
                        .cabinZone(aircraftDataGenerator.getRandomCabinZone())
                        .temperatureC(aircraftDataGenerator.generateRealisticTemperature())
                        .build())
                .build();
    }
    
    public VHFMessageDto generateHighTemperatureMessage() {
        return VHFMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .aircraftId(aircraftDataGenerator.getRandomAircraftId())
                .sensorType("CABIN_TEMP_V1")
                .schemaVersion("1.0")
                .ts(Instant.now())
                .payload(VHFMessageDto.TemperaturePayloadDto.builder()
                        .cabinZone(aircraftDataGenerator.getRandomCabinZone())
                        .temperatureC(aircraftDataGenerator.generateHighTemperature())
                        .build())
                .build();
    }
    
    public VHFMessageDto generateLowTemperatureMessage() {
        return VHFMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .aircraftId(aircraftDataGenerator.getRandomAircraftId())
                .sensorType("CABIN_TEMP_V1")
                .schemaVersion("1.0")
                .ts(Instant.now())
                .payload(VHFMessageDto.TemperaturePayloadDto.builder()
                        .cabinZone(aircraftDataGenerator.getRandomCabinZone())
                        .temperatureC(aircraftDataGenerator.generateLowTemperature())
                        .build())
                .build();
    }
    
    public VHFMessageDto generateTrendBasedMessage(String aircraftId, double previousTemp) {
        return VHFMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .aircraftId(aircraftId)
                .sensorType("CABIN_TEMP_V1")
                .schemaVersion("1.0")
                .ts(Instant.now())
                .payload(VHFMessageDto.TemperaturePayloadDto.builder()
                        .cabinZone(aircraftDataGenerator.getRandomCabinZone())
                        .temperatureC(aircraftDataGenerator.generateTemperatureTrend(aircraftId, previousTemp))
                        .build())
                .build();
    }
}
