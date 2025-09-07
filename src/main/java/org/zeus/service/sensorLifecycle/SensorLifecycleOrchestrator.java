package org.zeus.service.sensorLifecycle;

import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorType;

import java.util.List;

public interface SensorLifecycleOrchestrator {
    SensorResponse registerSensor(SensorRegistrationRequest request);
    SensorResponse getSensorById(String sensorId);
    List<SensorType> getSupportedSensorTypes();
    List<SensorResponse> getSensors();
}
