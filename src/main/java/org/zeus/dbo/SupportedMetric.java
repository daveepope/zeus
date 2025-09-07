package org.zeus.dbo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;

/**
 * Entity representing the supported_metrics join table.
 * It's used to model the many-to-many relationship between SensorType and MetricType.
 */
@Entity
@Table(name = "supported_metrics")
public class SupportedMetric {

    @EmbeddedId
    private SupportedMetricId id;

    @ManyToOne
    @JoinColumn(name = "sensor_type_id", insertable = false, updatable = false)
    private org.zeus.dbo.SensorType dbSensorType;

    @ManyToOne
    @JoinColumn(name = "metric_type_id", insertable = false, updatable = false)
    private org.zeus.dbo.MetricType dbMetricType;

    // Getters and setters
    public SupportedMetricId getId() {
        return id;
    }

    public void setId(SupportedMetricId id) {
        this.id = id;
    }

    public org.zeus.dbo.SensorType getDbSensorType() {
        return dbSensorType;
    }

    public void setDbSensorType(org.zeus.dbo.SensorType dbSensorType) {
        this.dbSensorType = dbSensorType;
    }

    public org.zeus.dbo.MetricType getDbMetricType() {
        return dbMetricType;
    }

    public void setDbMetricType(org.zeus.dbo.MetricType dbMetricType) {
        this.dbMetricType = dbMetricType;
    }
}
