package org.zeus.dbo;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "sensor_event_types")
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "eventTypeId")
public class SensorEventType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_type_id")
    private Integer eventTypeId;

    @Column(name = "type_name", nullable = false, unique = true)
    private String typeName;
}