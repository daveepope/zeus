package org.zeus.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zeus.dbo.MetricType;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorAuditEvent;
import org.zeus.dbo.SensorMeasurement;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.mapper.report.ReportingMapper;
import org.zeus.model.AggregatedMeasurementResponse;
import org.zeus.model.StatisticType;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.repository.telemetry.MeasurementRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingOrchestratorImplTest {

    @Mock
    private MeasurementRepository measurementRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private ReportingMapper reportingMapper;

    @InjectMocks
    private ReportingOrchestratorImpl reportingOrchestrator;

    private List<String> sensorIds;
    private List<org.zeus.model.MetricType> metricTypes;
    private AggregatedMeasurementResponse mockResponse;

    @BeforeEach
    void setUp() {
        sensorIds = List.of("sensor-1");
        metricTypes = List.of(org.zeus.model.MetricType.TEMPERATURE);

        mockResponse = new AggregatedMeasurementResponse();
        mockResponse.setResults(new ArrayList<>());
    }

    private SensorMeasurement createMockMeasurement(String sensorId, String metricName, double value) {
        Sensor sensor = new Sensor();
        sensor.setSensorId(sensorId);
        SensorAuditEvent event = new SensorAuditEvent();
        event.setSensor(sensor);
        MetricType metricType = new MetricType();
        metricType.setTypeName(metricName);
        SensorMeasurement measurement = new SensorMeasurement();
        measurement.setEvent(event);
        measurement.setMetricType(metricType);
        measurement.setMeasurementValue(BigDecimal.valueOf(value));
        return measurement;
    }

    @Test
    void getAggregatedMeasurements_withValidInputs_shouldReturnAggregatedResponse() {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now();
        StatisticType statistic = StatisticType.AVERAGE;

        List<SensorMeasurement> rawMeasurements = List.of(
                createMockMeasurement("sensor-1", "TEMPERATURE", 10.0),
                createMockMeasurement("sensor-1", "TEMPERATURE", 20.0)
        );

        when(sensorRepository.countBySensorIdIn(sensorIds)).thenReturn((long) sensorIds.size());
        when(measurementRepository.findByQuery(anyList(), anyList(), any(), any())).thenReturn(rawMeasurements);
        when(reportingMapper.toApiMetricType(any(MetricType.class))).thenReturn(org.zeus.model.MetricType.TEMPERATURE);
        when(reportingMapper.toApiAggregatedMeasurementResponse(any(), any())).thenReturn(mockResponse);

        AggregatedMeasurementResponse response = reportingOrchestrator.getAggregatedMeasurements(sensorIds, metricTypes, statistic, startDate, endDate);

        assertThat(response).isNotNull();
    }

    @Test
    void getAggregatedMeasurements_whenDatesAreNull_shouldDefaultToLast7Days() {
        StatisticType statistic = StatisticType.MAX;
        List<SensorMeasurement> rawMeasurements = List.of(createMockMeasurement("sensor-1", "TEMPERATURE", 25.0));

        when(sensorRepository.countBySensorIdIn(sensorIds)).thenReturn(1L);
        when(measurementRepository.findByQuery(any(), any(), any(), any())).thenReturn(rawMeasurements);
        when(reportingMapper.toApiMetricType(any(MetricType.class))).thenReturn(org.zeus.model.MetricType.TEMPERATURE);
        when(reportingMapper.toApiAggregatedMeasurementResponse(any(), any())).thenReturn(mockResponse);

        AggregatedMeasurementResponse response = reportingOrchestrator.getAggregatedMeasurements(sensorIds, metricTypes, statistic, null, null);

        assertThat(response).isNotNull();

        ArgumentCaptor<OffsetDateTime> startDateCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endDateCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(measurementRepository).findByQuery(any(), any(), startDateCaptor.capture(), endDateCaptor.capture());

        OffsetDateTime capturedStart = startDateCaptor.getValue();
        OffsetDateTime capturedEnd = endDateCaptor.getValue();

        assertThat(capturedEnd).isCloseTo(OffsetDateTime.now(ZoneOffset.UTC), org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
        assertThat(capturedStart).isCloseTo(capturedEnd.minusDays(7), org.assertj.core.api.Assertions.within(1, ChronoUnit.SECONDS));
    }

    @Test
    void getAggregatedMeasurements_whenSensorNotFound_shouldThrowSensorNotFoundException() {
        List<String> requestedSensorIds = List.of("sensor-1", "non-existent-sensor");
        StatisticType statistic = StatisticType.MIN;
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(1);
        OffsetDateTime endDate = OffsetDateTime.now();

        when(sensorRepository.countBySensorIdIn(requestedSensorIds)).thenReturn(1L);

        assertThrows(SensorNotFoundException.class, () -> {
            reportingOrchestrator.getAggregatedMeasurements(requestedSensorIds, metricTypes, statistic, startDate, endDate);
        });

        verify(measurementRepository, never()).findByQuery(any(), any(), any(), any());
    }
}