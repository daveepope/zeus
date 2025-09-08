package org.zeus.repository.telemetry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zeus.dbo.SupportedMetric;
import org.zeus.dbo.SupportedMetricId;

import java.util.List;

@Repository
public interface SupportedMetricsRepository extends JpaRepository<SupportedMetric, SupportedMetricId> {

    /**
     * Finds all supported metrics for a given sensor type ID.
     *
     * @param sensorTypeId The ID of the sensor type.
     * @return A list of SupportedMetric entities.
     */
    List<SupportedMetric> findById_SensorTypeId(Integer sensorTypeId);
}
