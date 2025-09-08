package org.zeus.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.zeus.api.ReportingApi;
import org.zeus.model.AggregatedMeasurementResponse;
import org.zeus.model.MetricType;
import org.zeus.model.StatisticType;
import org.zeus.service.report.ReportingOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set; // Use Set instead of List

@RestController
@RequiredArgsConstructor
public class ReportingController implements ReportingApi {

    private static final Logger log = LoggerFactory.getLogger(ReportingController.class);
    private final ReportingOrchestrator reportingOrchestrator;

    /**
     * GET /reports/measurements
     * Query and aggregate sensor measurement data.
     */
    @Override
    public ResponseEntity<AggregatedMeasurementResponse> getAggregatedMeasurements(
            @NotNull @Valid Set<MetricType> metricTypes,
            @NotNull @Valid StatisticType statistic,
            @Valid Set<String> sensorIds,
            @Valid OffsetDateTime startDate,
            @Valid OffsetDateTime endDate) {

        log.info("Received request for aggregated measurements. Sensors: {}, Metrics: {}, Statistic: {}, Start: {}, End: {}",
                sensorIds, metricTypes, statistic, startDate, endDate);

        // Delegation of business logic to the ReportingOrchestrator
        // Note: The orchestrator's method needs to be adjusted to accept Set instead of List.
        AggregatedMeasurementResponse response = reportingOrchestrator.getAggregatedMeasurements(
                sensorIds != null ? List.copyOf(sensorIds) : null, // Convert Set to List for the orchestrator
                List.copyOf(metricTypes), // Convert Set to List for the orchestrator
                statistic,
                startDate,
                endDate);

        log.info("Successfully generated aggregated report.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}