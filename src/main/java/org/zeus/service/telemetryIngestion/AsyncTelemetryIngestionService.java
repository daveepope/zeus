package org.zeus.service.telemetryIngestion;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.zeus.model.MeasurementRequest;

@Service
@RequiredArgsConstructor
public class AsyncTelemetryIngestionService {

    private static final Logger log = LoggerFactory.getLogger(AsyncTelemetryIngestionService.class);

    private final TelemetryIngestionOrchestrator telemetryIngestionOrchestrator;

    @Async
    public void startIngestion(MeasurementRequest measurementRequest) {
        try {
            log.info("Handing off ingestion to background thread for sensor ID: {}", measurementRequest.getSensorId());
            telemetryIngestionOrchestrator.ingestMeasurements(measurementRequest);

        } catch (Exception e) {
            log.error("Async ingestion process failed for sensor ID: " + measurementRequest.getSensorId(), e);
        }
    }
}