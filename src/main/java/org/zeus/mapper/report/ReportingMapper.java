package org.zeus.mapper.report;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import org.zeus.dbo.MetricType;
import org.zeus.model.AggregatedMeasurementResponse;
import org.zeus.model.AggregatedMetric;
import org.zeus.model.AggregatedSensorResult;
import org.zeus.model.StatisticType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface ReportingMapper {

    /**
     * Maps an aggregated value to the API's AggregatedMetric DTO.
     * <p>
     * The @Mapping annotations explicitly define which source parameter maps
     * to which target property, resolving MapStruct's ambiguity.
     * The `BigDecimal` source is automatically converted to `Double` for the target.
     *
     * @param metricType      The type of metric.
     * @param statistic       The statistic applied (e.g., average, min, max).
     * @param value           The aggregated value from the orchestrator (as a BigDecimal).
     * @return The corresponding AggregatedMetric DTO.
     */
    @Mapping(target = "metricType", source = "metricType")
    @Mapping(target = "statistic", source = "statistic")
    @Mapping(target = "value", source = "value")
    AggregatedMetric toApiAggregatedMetric(org.zeus.model.MetricType metricType, StatisticType statistic, BigDecimal value);

    /**
     * Maps a sensor's ID and its list of aggregated metrics to the AggregatedSensorResult DTO.
     *
     * @param sensorId         The unique ID of the sensor.
     * @param aggregatedMetrics A list of aggregated metrics for the sensor.
     * @return The corresponding AggregatedSensorResult DTO.
     */
    AggregatedSensorResult toApiAggregatedSensorResult(String sensorId, List<AggregatedMetric> aggregatedMetrics);

    /**
     * Maps a list of aggregated sensor results and a query timestamp to the final API response DTO.
     *
     * @param results          The list of aggregated results.
     * @param queryTimestamp   The timestamp when the query was executed.
     * @return The corresponding AggregatedMeasurementResponse DTO.
     */
    AggregatedMeasurementResponse toApiAggregatedMeasurementResponse(List<AggregatedSensorResult> results, OffsetDateTime queryTimestamp);

    /**
     * Maps a database MetricType entity to the API MetricType enum.
     *
     * @param dbMetricType The database entity representation of a metric type.
     * @return The corresponding API enum.
     */
    default org.zeus.model.MetricType toApiMetricType(MetricType dbMetricType) {
        if (dbMetricType == null || dbMetricType.getTypeName() == null) {
            return null;
        }
        return org.zeus.model.MetricType.fromValue(dbMetricType.getTypeName());
    }
}