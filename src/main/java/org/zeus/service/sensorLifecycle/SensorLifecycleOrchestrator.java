package org.zeus.service.sensorLifecycle;

import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;

public interface SensorLifecycleOrchestrator {
    SensorResponse registerSensor(SensorRegistrationRequest request);
    SensorResponse getSensorById(String sensorId);
}
