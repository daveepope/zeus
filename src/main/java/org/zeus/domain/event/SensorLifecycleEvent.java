package org.zeus.domain.event;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class SensorLifecycleEvent {
    String sensorId;
    String status;
    OffsetDateTime timestamp;
}