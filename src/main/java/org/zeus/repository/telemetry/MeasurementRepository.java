package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SensorMeasurement;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MeasurementRepository extends JpaRepository<SensorMeasurement, Long> {

    /**
     * Retrieves all sensor measurements that match a given set of filters.
     * The query joins the necessary tables to filter by sensor ID and metric type,
     * and a timestamp range.
     *
     * @param sensorIds A list of sensor IDs. If null or empty, the query will not filter by sensor.
     * @param metricTypeNames A list of metric type names.
     * @param startDate The start of the timestamp range (inclusive).
     * @param endDate The end of the timestamp range (inclusive).
     * @return A list of {@link SensorMeasurement} entities that match the criteria.
     */
    @Query("SELECT sm FROM SensorMeasurement sm JOIN sm.event sae JOIN sm.metricType mt " +
            "WHERE (:sensorIds IS NULL OR sae.sensor.sensorId IN :sensorIds) " +
            "AND mt.typeName IN :metricTypeNames AND sm.recordedAt BETWEEN :startDate AND :endDate")
    List<SensorMeasurement> findByQuery(
            @Param("sensorIds") List<String> sensorIds,
            @Param("metricTypeNames") List<String> metricTypeNames,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);
}