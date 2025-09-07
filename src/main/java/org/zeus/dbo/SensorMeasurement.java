package org.zeus.dbo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    private SensorEvent event;

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