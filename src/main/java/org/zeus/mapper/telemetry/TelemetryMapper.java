package org.zeus.mapper.telemetry;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorMeasurement;
import org.zeus.dbo.MetricType;
import org.zeus.dbo.SensorAuditEvent;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

    @Mapping(target = "measurementId", ignore = true)

    @Mapping(target = "event", source = "auditEvent")

    @Mapping(target = "measurementValue", source = "apiMeasurement.measurementValue")

    @Mapping(target = "metricType", source = "metricType")

    @Mapping(target = "recordedAt", source = "eventTimestamp")

    SensorMeasurement toEntity(org.zeus.model.Measurement apiMeasurement, Sensor sensor, OffsetDateTime eventTimestamp, MetricType metricType, SensorAuditEvent auditEvent);
}