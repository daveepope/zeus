package org.zeus.dbo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.UniqueConstraint;
import lombok.Setter;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "sensor_measurements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "metric_type_id"})
})
@Getter
@Setter
@ToString(exclude = {"event"})
public class SensorMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private SensorAuditEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_type_id", nullable = false)
    private MetricType metricType;

    @Column(name = "measurement_value", nullable = false, precision = 15, scale = 6)
    private BigDecimal measurementValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorMeasurement that = (SensorMeasurement) o;
        return measurementId != null && measurementId.equals(that.measurementId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}