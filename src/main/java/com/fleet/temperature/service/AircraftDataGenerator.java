package com.fleet.temperature.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class AircraftDataGenerator {
    
    private final Random random = new Random();
    
    private static final List<String> AIRCRAFT_IDS = Arrays.asList(
        "TC-ABC", "TC-DEF", "TC-GHI", "TC-JKL", "TC-MNO", "TC-PQR", "TC-STU", "TC-VWX",
        "TC-AA1", "TC-BB2", "TC-CC3", "TC-DD4", "TC-EE5", "TC-FF6", "TC-GG7", "TC-HH8",
        "TC-II9", "TC-JJ0", "TC-KK1", "TC-LL2", "TC-MM3", "TC-NN4", "TC-OO5", "TC-PP6"
    );
    
    private static final List<String> AIRCRAFT_MODELS = Arrays.asList(
        "Boeing 737-800", "Boeing 737-900", "Boeing 777-300ER", "Boeing 787-9",
        "Airbus A320", "Airbus A321", "Airbus A330-300", "Airbus A350-900"
    );
    
    private static final List<String> CABIN_ZONES = Arrays.asList(
        "FWD", "MID", "AFT"
    );
    
    public String getRandomAircraftId() {
        return AIRCRAFT_IDS.get(random.nextInt(AIRCRAFT_IDS.size()));
    }
    
    public String getRandomAircraftModel() {
        return AIRCRAFT_MODELS.get(random.nextInt(AIRCRAFT_MODELS.size()));
    }
    
    public String getRandomCabinZone() {
        return CABIN_ZONES.get(random.nextInt(CABIN_ZONES.size()));
    }
    
    public double generateRealisticTemperature() {
        // Normal cabin temperature range: 18-32°C with Gaussian distribution
        return Math.max(18.0, Math.min(32.0, 22.0 + (random.nextGaussian() * 3.0)));
    }
    
    public double generateHighTemperature() {
        // High temperature range: 28-35°C
        return 28.0 + (random.nextDouble() * 7.0);
    }
    
    public double generateLowTemperature() {
        // Low temperature range: 15-18°C
        return 15.0 + (random.nextDouble() * 3.0);
    }
    
    public double generateTemperatureVariation(double baseTemperature) {
        // Add small variation to existing temperature
        return baseTemperature + (-2.0 + (random.nextDouble() * 4.0));
    }
    
    public int getAircraftCount() {
        return AIRCRAFT_IDS.size();
    }
    
    public double generateTemperatureTrend(String aircraftId, double previousTemp) {
        // Generate trend based on aircraft ID hash for consistency
        int hash = Math.abs(aircraftId.hashCode());
        int trend = (hash % 10) - 5; // -5 to +5 range
        return previousTemp + (trend * 0.1);
    }
}
