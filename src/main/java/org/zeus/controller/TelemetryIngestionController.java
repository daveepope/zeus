package org.zeus.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.zeus.api.TelemetryIngestionApi;
import org.zeus.model.MeasurementRequest;
import org.zeus.service.telemetryIngestion.TelemetryIngestionOrchestrator;

@RestController
@RequiredArgsConstructor
public class TelemetryIngestionController implements TelemetryIngestionApi {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestionController.class);
    private final TelemetryIngestionOrchestrator telemetryIngestionOrchestrator;

    /**
     * POST /measurements
     * Submit new sensor measurements
     *
     * @param measurementRequest A list of new measurements from a sensor. (required)
     * @return Measurements accepted for processing. (status code 202)
     * or Invalid input provided, or a measurement type is not supported by the sensor. (status code 400)
     * or Sensor not found. (status code 404)
     */
    @Override
    public ResponseEntity<Void> ingestMeasurements(MeasurementRequest measurementRequest) {
        log.info("Received request to ingest measurements for sensor ID: {}", measurementRequest.getSensorId());
        telemetryIngestionOrchestrator.ingestMeasurements(measurementRequest);
        log.info("Measurements from sensor ID: {} accepted for processing.", measurementRequest.getSensorId());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}