package org.zeus.mapper.telemetry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorMeasurement;
import org.zeus.dbo.MetricType;
import org.zeus.dbo.SensorAuditEvent;
import java.time.OffsetDateTime;

/**
 * Mapper interface for converting telemetry-related DTOs and database entities.
 * Utilizes MapStruct for boilerplate code generation.
 */
@Mapper(componentModel = "spring")
public interface TelemetryMapper {

    /**
     * Maps an API Measurement DTO to a SensorMeasurement database entity.
     * This is used during the telemetry ingestion process.
     *
     * @param apiMeasurement The Measurement object from the API request.
     * @param sensor         The Sensor entity to which this measurement belongs.
     * @param eventTimestamp The authoritative timestamp for when the measurement was recorded.
     * @param metricType     The MetricType entity corresponding to the measurement.
     * @param auditEvent     The parent SensorAuditEvent to which this measurement is linked.
     * @return A new SensorMeasurement entity, ready to be persisted.
     */
    @Mapping(target = "measurementId", ignore = true)
    @Mapping(target = "event", source = "auditEvent")
    @Mapping(target = "measurementValue", source = "apiMeasurement.measurementValue")
    @Mapping(target = "metricType", source = "metricType")
    @Mapping(target = "recordedAt", source = "eventTimestamp")
    SensorMeasurement toEntity(org.zeus.model.Measurement apiMeasurement, Sensor sensor, OffsetDateTime eventTimestamp, MetricType metricType, SensorAuditEvent auditEvent);
}