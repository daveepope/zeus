package org.zeus.mapper.sensor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorType;
import org.zeus.dbo.SupportedMetric;
import org.zeus.model.MetricType;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;

import java.util.ArrayList;
import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface SensorMapper {

    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sensorType", source = "sensorType")
    @Mapping(target = "state", source = "initialState")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)     // Set manually in the service
    @Mapping(target = "events", ignore = true)
    Sensor toEntity(SensorRegistrationRequest request, SensorType sensorType, org.zeus.dbo.SensorState initialState);


    @Mapping(target = "sensorType", source = "sensor.sensorType.typeName")
    @Mapping(target = "state", source = "sensor.state.stateName")
    @Mapping(target = "supportedMetrics", ignore = true)
    SensorResponse toResponse(Sensor sensor);

    default SensorState mapStateNameToEnum(String stateName) {
        if (stateName == null) {
            return null;
        }
        return SensorState.valueOf(stateName);
    }

    @Named("metricTypeToEnum")
    default org.zeus.model.MetricType mapMetricType(org.zeus.dbo.MetricType metricType) {
        if (metricType == null || metricType.getTypeName() == null) {
            return null;
        }
        return org.zeus.model.MetricType.valueOf(metricType.getTypeName());
    }

    default List<MetricType> mapMetricTypes(List<SupportedMetric> supportedMetrics){
        var apiMetrics = new ArrayList<MetricType>();
        for (var supportedMetric : supportedMetrics) {
            apiMetrics.add(mapMetricType(supportedMetric.getDbMetricType()));
        }

        return apiMetrics;
    }

    default List<org.zeus.model.SensorType> mapToApiSensorTypes(List<SensorType> supportedDbTypes) {
        var apiSensorTypes = new ArrayList<org.zeus.model.SensorType>();

        for (var dbType : supportedDbTypes) {
            apiSensorTypes.add(org.zeus.model.SensorType.fromValue(dbType.getTypeName())); // will get caught by global exception handler
        }

        return apiSensorTypes;
    }
}

