package org.zeus.service.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.dbo.SensorMeasurement;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.model.*;
import org.zeus.repository.telemetry.MeasurementRepository;
import org.zeus.repository.sensor.SensorRepository;
import org.zeus.mapper.report.ReportingMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingOrchestratorImpl implements ReportingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ReportingOrchestratorImpl.class);
    private static final long DEFAULT_DAYS_FOR_LATEST_DATA = 7;
    private static final int DECIMAL_SCALE = 6; // Matching the schema precision

    private final MeasurementRepository measurementRepository;
    private final SensorRepository sensorRepository;
    private final ReportingMapper reportingMapper;

    @Override
    @Cacheable(cacheNames = "reportResults")
    public AggregatedMeasurementResponse getAggregatedMeasurements(
            List<String> sensorIds,
            List<MetricType> metricTypes,
            StatisticType statistic,
            OffsetDateTime startDate,
            OffsetDateTime endDate) {

        log.info("Executing reporting query with in-memory aggregation. Sensor IDs: {}, Metric Types: {}, Statistic: {}, Date Range: {} to {}",
                sensorIds, metricTypes, statistic, startDate, endDate);

        if (sensorIds != null && !sensorIds.isEmpty()) {
            long existingSensorsCount = sensorRepository.countBySensorIdIn(sensorIds);
            if (existingSensorsCount != sensorIds.size()) {
                throw new SensorNotFoundException("One or more of the requested sensors were not found.");
            }
        }

        if (startDate == null || endDate == null) {
            endDate = OffsetDateTime.now(ZoneOffset.UTC);
            startDate = endDate.minusDays(DEFAULT_DAYS_FOR_LATEST_DATA);
            log.info("No date range provided, defaulting to the last {} days from {}", DEFAULT_DAYS_FOR_LATEST_DATA, endDate);
        }

        List<String> metricTypeNames = metricTypes.stream()
                .map(MetricType::toString)
                .collect(Collectors.toList());

        List<SensorMeasurement> rawMeasurements = measurementRepository.findByQuery(sensorIds, metricTypeNames, startDate, endDate);
        log.info("Query returned {} raw measurements.", rawMeasurements.size()); // Add this line

        Map<String, Map<MetricType, List<SensorMeasurement>>> groupedMeasurements = rawMeasurements.stream()
                .collect(Collectors.groupingBy(
                        measurement -> measurement.getEvent().getSensor().getSensorId(),
                        Collectors.groupingBy(
                                measurement -> reportingMapper.toApiMetricType(measurement.getMetricType())
                        )
                ));

        List<AggregatedSensorResult> aggregatedResults = groupedMeasurements.entrySet().stream()
                .map(sensorEntry -> {
                    String sensorId = sensorEntry.getKey();
                    Map<MetricType, List<SensorMeasurement>> metricMap = sensorEntry.getValue();

                    List<AggregatedMetric> aggregatedMetrics = metricMap.entrySet().stream()
                            .map(metricEntry -> {
                                MetricType metricType = metricEntry.getKey();
                                List<SensorMeasurement> measurements = metricEntry.getValue();

                                BigDecimal aggregatedValue = BigDecimal.ZERO;
                                if (!measurements.isEmpty()) {
                                    switch (statistic) {
                                        case MIN -> aggregatedValue = measurements.stream()
                                                .map(SensorMeasurement::getMeasurementValue)
                                                .min(BigDecimal::compareTo)
                                                .orElse(BigDecimal.ZERO);
                                        case MAX -> aggregatedValue = measurements.stream()
                                                .map(SensorMeasurement::getMeasurementValue)
                                                .max(BigDecimal::compareTo)
                                                .orElse(BigDecimal.ZERO);
                                        case SUM -> aggregatedValue = measurements.stream()
                                                .map(SensorMeasurement::getMeasurementValue)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        case AVERAGE -> {
                                            BigDecimal sum = measurements.stream()
                                                    .map(SensorMeasurement::getMeasurementValue)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            aggregatedValue = sum.divide(new BigDecimal(measurements.size()), DECIMAL_SCALE, RoundingMode.HALF_UP);
                                        }
                                    }
                                }

                                // The API spec defines the value as a 'double', so we convert the final BigDecimal.
                                // The mapper will handle this conversion.
                                return reportingMapper.toApiAggregatedMetric(metricType, statistic, aggregatedValue);
                            })
                            .collect(Collectors.toList());

                    return reportingMapper.toApiAggregatedSensorResult(sensorId, aggregatedMetrics);
                })
                .collect(Collectors.toList());

        AggregatedMeasurementResponse response = reportingMapper.toApiAggregatedMeasurementResponse(aggregatedResults, OffsetDateTime.now(ZoneOffset.UTC));

        log.info("Successfully generated report with {} sensor results.", response.getResults().size());
        return response;
    }
}