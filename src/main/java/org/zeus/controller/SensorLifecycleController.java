package org.zeus.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.zeus.api.SensorLifecycleApi;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.service.sensorLifecycle.SensorLifecycleOrchestrator;

@RestController
@RequiredArgsConstructor
public class SensorLifecycleController implements SensorLifecycleApi {

    private static final Logger log = LoggerFactory.getLogger(SensorLifecycleController.class);
    private final SensorLifecycleOrchestrator sensorLifecycleOrchestrator;

    @Override
    public ResponseEntity<SensorResponse> registerSensor(SensorRegistrationRequest sensorRegistrationRequest) {
        log.info("Received request to register a new sensor.");
        SensorResponse newSensor = sensorLifecycleOrchestrator.registerSensor(sensorRegistrationRequest);
        log.info("Successfully registered sensor with ID: {}. Returning 201 Created.", newSensor.getSensorId());
        return new ResponseEntity<>(newSensor, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SensorResponse> getSensorById(String sensorId) {
        log.info("Received request to get sensor by ID: {}", sensorId);
        SensorResponse sensor = sensorLifecycleOrchestrator.getSensorById(sensorId);
        log.info("Successfully retrieved sensor with ID: {}. Returning 200 OK.", sensor.getSensorId());
        return ResponseEntity.ok(sensor);
    }
}

