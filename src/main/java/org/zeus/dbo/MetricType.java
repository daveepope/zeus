package org.zeus.dbo;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "metric_types")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "metricTypeId")
public class MetricType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_type_id")
    private Integer metricTypeId;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;

    @Column(name = "unit_symbol", nullable = false)
    private String unitSymbol;
}