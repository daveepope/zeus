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

/**
 * Mapper interface for converting between sensor-related DTOs and database entities.
 * Utilizes MapStruct for boilerplate code generation.
 */
@Component
@Mapper(componentModel = "spring")
public interface SensorMapper {

    /**
     * Maps a SensorRegistrationRequest DTO to a Sensor database entity.
     * Several fields are ignored as they are set manually in the service layer
     * during the registration process (e.g., timestamps, audit data).
     *
     * @param request      The incoming sensor registration request from the API.
     * @param sensorType   The corresponding SensorType database entity.
     * @param initialState The initial SensorState database entity for the new sensor.
     * @return A new Sensor entity, ready to be persisted.
     */
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "sensorType", source = "sensorType")
    @Mapping(target = "state", source = "initialState")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "lastUpdatedBy", ignore = true)     // Set manually in the service
    @Mapping(target = "events", ignore = true)
    Sensor toEntity(SensorRegistrationRequest request, SensorType sensorType, org.zeus.dbo.SensorState initialState);


    /**
     * Maps a Sensor database entity to a SensorResponse DTO for API responses.
     * The supportedMetrics field is ignored and intended to be populated separately
     * in the service layer.
     *
     * @param sensor The Sensor entity retrieved from the database.
     * @return A SensorResponse DTO suitable for sending to the client.
     */
    @Mapping(target = "sensorType", source = "sensor.sensorType.typeName")
    @Mapping(target = "state", source = "sensor.state.stateName")
    @Mapping(target = "supportedMetrics", ignore = true)
    SensorResponse toResponse(Sensor sensor);

    /**
     * Converts a string representation of a sensor state to the corresponding SensorState enum.
     *
     * @param stateName The state name as a string (e.g., "CONNECTED").
     * @return The matching SensorState enum, or null if the input is null.
     */
    default SensorState mapStateNameToEnum(String stateName) {
        if (stateName == null) {
            return null;
        }
        return SensorState.valueOf(stateName);
    }

    /**
     * Converts a MetricType database entity to the corresponding MetricType API enum.
     *
     * @param metricType The MetricType entity from the database.
     * @return The matching MetricType enum, or null if the input or its type name is null.
     */
    @Named("metricTypeToEnum")
    default org.zeus.model.MetricType mapMetricType(org.zeus.dbo.MetricType metricType) {
        if (metricType == null || metricType.getTypeName() == null) {
            return null;
        }
        return org.zeus.model.MetricType.valueOf(metricType.getTypeName());
    }

    /**
     * Maps a list of SupportedMetric join-table entities to a list of MetricType API enums.
     *
     * @param supportedMetrics The list of SupportedMetric entities from the database.
     * @return A list of MetricType enums representing the metrics supported by a sensor.
     */
    default List<MetricType> mapMetricTypes(List<SupportedMetric> supportedMetrics){
        var apiMetrics = new ArrayList<MetricType>();
        for (var supportedMetric : supportedMetrics) {
            apiMetrics.add(mapMetricType(supportedMetric.getDbMetricType()));
        }

        return apiMetrics;
    }

    /**
     * Maps a list of SensorType database entities to a list of SensorType API enums.
     *
     * @param supportedDbTypes The list of SensorType entities from the database.
     * @return A list of SensorType enums for use in API responses.
     */
    default List<org.zeus.model.SensorType> mapToApiSensorTypes(List<SensorType> supportedDbTypes) {
        var apiSensorTypes = new ArrayList<org.zeus.model.SensorType>();

        for (var dbType : supportedDbTypes) {
            apiSensorTypes.add(org.zeus.model.SensorType.fromValue(dbType.getTypeName())); // will get caught by global exception handler
        }

        return apiSensorTypes;
    }
}