package org.zeus.dbo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key class for the SupportedMetric entity.
 * It maps to the primary key of the supported_metrics table.
 */
@Embeddable
public class SupportedMetricId implements Serializable {

    @Column(name = "sensor_type_id")
    private Integer sensorTypeId;

    @Column(name = "metric_type_id")
    private Integer metricTypeId;

    // Getters, setters, equals, and hashCode
    public Integer getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(Integer sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public Integer getMetricTypeId() {
        return metricTypeId;
    }

    public void setMetricTypeId(Integer metricTypeId) {
        this.metricTypeId = metricTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportedMetricId that = (SupportedMetricId) o;
        return Objects.equals(sensorTypeId, that.sensorTypeId) &&
                Objects.equals(metricTypeId, that.metricTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorTypeId, metricTypeId);
    }
}
