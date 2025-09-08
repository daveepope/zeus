package org.zeus.service.telemetryIngestion;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.dbo.SensorMeasurement;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.MetricType;
import org.zeus.dbo.SensorAuditEvent;
import org.zeus.dbo.AuditEventType;
import org.zeus.exception.InvalidMeasurementException;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.mapper.telemetry.TelemetryMapper;
import org.zeus.model.MeasurementRequest;
import org.zeus.repository.telemetry.MeasurementRepository;
import org.zeus.repository.telemetry.MetricTypeRepository;
import org.zeus.repository.telemetry.SupportedMetricsRepository;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.telemetry.AuditEventTypeRepository;
import org.zeus.repository.telemetry.AuditEventRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TelemetryIngestionOrchestratorImpl implements TelemetryIngestionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestionOrchestratorImpl.class);

    private final SensorRepository sensorRepository;
    private final MeasurementRepository measurementRepository;
    private final SupportedMetricsRepository supportedMetricsRepository;
    private final TelemetryMapper telemetryMapper;
    private final AuditEventTypeRepository auditEventTypeRepository;
    private final AuditEventRepository auditEventRepository;
    private final MetricTypeRepository metricTypeRepository;

    @Override
    @Transactional
    public void ingestMeasurements(MeasurementRequest request) {
        log.info("Starting ingestion process for measurements from sensor ID: {}", request.getSensorId());

        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> {
                    log.warn("Sensor not found with ID: {}", request.getSensorId());
                    return new SensorNotFoundException("Sensor with ID '" + request.getSensorId() + "' not found.");
                });

        Set<String> supportedMetricNames = supportedMetricsRepository
                .findById_SensorTypeId(sensor.getSensorType().getSensorTypeId())
                .stream()
                .map(supportedMetric -> supportedMetric.getDbMetricType().getTypeName())
                .collect(Collectors.toSet());

        AuditEventType measurementEventType = auditEventTypeRepository.findByTypeName(org.zeus.model.AuditEventType.MEASUREMENT.toString())
                .orElseThrow(() -> new IllegalStateException("Audit event type 'MEASUREMENT' not found in database."));

        List<SensorMeasurement> measurementsToSave = new ArrayList<>();
        List<SensorAuditEvent> auditEvents = new ArrayList<>();

        for (org.zeus.model.Measurement apiMeasurement : request.getMeasurements()) {
            String metricName = apiMeasurement.getMetricType().getValue();

            if (!supportedMetricNames.contains(metricName)) {
                log.error("Received unsupported metric type '{}' for sensor {}", metricName, request.getSensorId());
                throw new InvalidMeasurementException("The metric type '" + metricName + "' is not supported by sensor '" + request.getSensorId() + "'.");
            }

            // Corrected lookup for the DBO MetricType
            MetricType dboMetricType = metricTypeRepository.findByTypeName(metricName)
                    .orElseThrow(() -> {
                        log.error("Metric type '{}' not found in database, but is configured as supported.", metricName);
                        return new IllegalStateException("Metric type '" + metricName + "' is configured as supported by sensor '" + request.getSensorId() + "' but not found in the database.");
                    });

            var auditEvent = new SensorAuditEvent();
            auditEvent.setSensor(sensor);
            auditEvent.setEventTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            auditEvent.setEventType(measurementEventType);
            auditEvents.add(auditEvent);

            var measurementToSave = telemetryMapper.toEntity(apiMeasurement, sensor, request.getEventTimestamp(), dboMetricType, auditEvent);
            measurementsToSave.add(measurementToSave);
        }

        auditEventRepository.saveAll(auditEvents);
        measurementRepository.saveAll(measurementsToSave);

        log.info("Successfully ingested and saved {} measurements for sensor ID: {}", measurementsToSave.size(), request.getSensorId());
    }
}