package org.zeus.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeus.api.SensorLifecycleApi;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorType;
import org.zeus.service.sensorLifecycle.SensorLifecycleOrchestrator;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SensorLifecycleController implements SensorLifecycleApi {

    private static final Logger log = LoggerFactory.getLogger(SensorLifecycleController.class);
    private final SensorLifecycleOrchestrator sensorLifecycleOrchestrator;

    @Override
    public ResponseEntity<SensorResponse> registerSensor(SensorRegistrationRequest sensorRegistrationRequest) {
        log.info("Received request to register a new sensor.");
        var newSensor = sensorLifecycleOrchestrator.registerSensor(sensorRegistrationRequest);
        log.info("Successfully registered sensor with ID: {}.", newSensor.getSensorId());
        return new ResponseEntity<>(newSensor, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SensorResponse> getSensorById(String sensorId) {
        log.info("Received request to get sensor by ID: {}", sensorId);
        var sensor = sensorLifecycleOrchestrator.getSensorById(sensorId);
        log.info("Successfully retrieved sensor with ID: {}.", sensor.getSensorId());
        return ResponseEntity.ok(sensor);
    }

    @Override
    public ResponseEntity<List<SensorResponse>> getSensors() {
        log.info("Received request to get all registered sensors");
        var sensors = sensorLifecycleOrchestrator.getSensors();
        log.info("Successfully retrieved all registered sensors");
        return ResponseEntity.ok(sensors);
    }

    @GetMapping("/sensors/types")
    public ResponseEntity<List<SensorType>> getSensorTypes() {
        log.info("Received request to get all supported sensor types.");
        var sensorTypes = sensorLifecycleOrchestrator.getSupportedSensorTypes();
        log.info("Successfully retrieved all supported sensor types.");
        return ResponseEntity.ok(sensorTypes);
    }
}

