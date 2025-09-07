package org.zeus.mapper.sensor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import org.zeus.dbo.Sensor;
import org.zeus.dbo.SensorType;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;

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
        // CHANGE 1: Using the fully qualified name to remove ambiguity
    Sensor toEntity(SensorRegistrationRequest request, SensorType sensorType, org.zeus.dbo.SensorState initialState);


    @Mapping(target = "sensorType", source = "sensor.sensorType.typeName")
    @Mapping(target = "state", source = "sensor.state.stateName")
    SensorResponse toResponse(Sensor sensor);


    // CHANGE 2: Correcting the helper method to match the generated enum
    // It is no longer needed as MapStruct can map String to Enum automatically,
    // but leaving it here as an explicit converter.
    default SensorState mapStateNameToEnum(String stateName) {
        if (stateName == null) {
            return null;
        }
        return SensorState.valueOf(stateName);
    }
}

