package org.zeus.service.report;

import org.zeus.model.AggregatedMeasurementResponse;
import org.zeus.model.MetricType;
import org.zeus.model.StatisticType;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReportingOrchestrator {

    /**
     * Retrieves and aggregates sensor measurement data based on specified criteria.
     *
     * @param sensorIds   A list of unique identifiers for the sensors to include in the report. If null or empty, all sensors should be considered.
     * @param metricTypes A list of the metric types (e.g., TEMPERATURE, HUMIDITY) to aggregate.
     * @param statistic   The type of statistical aggregation to apply to the data (e.g., AVERAGE, MIN, MAX).
     * @param startDate   The start timestamp of the time range for the query.
     * @param endDate     The end timestamp of the time range for the query.
     * @return An {@link AggregatedMeasurementResponse} containing the aggregated data for the requested sensors and metrics.
     */
    AggregatedMeasurementResponse getAggregatedMeasurements(List<String> sensorIds, List<MetricType> metricTypes, StatisticType statistic, OffsetDateTime startDate, OffsetDateTime endDate);
}