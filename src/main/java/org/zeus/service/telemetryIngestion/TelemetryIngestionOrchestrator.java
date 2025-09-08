package org.zeus.service.telemetryIngestion;

import org.zeus.model.MeasurementRequest;

public interface TelemetryIngestionOrchestrator {
    void ingestMeasurements(MeasurementRequest request);
}