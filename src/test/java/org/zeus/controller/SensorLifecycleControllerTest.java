package org.zeus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.zeus.exception.SensorAlreadyExistsException;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.model.HeartbeatRequest;
import org.zeus.model.SensorRegistrationRequest;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;
import org.zeus.model.SensorType;
import org.zeus.service.sensorLifecycle.AsyncSensorLifecycleOrchestrator;
import org.zeus.service.sensorLifecycle.SensorLifecycleOrchestrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorLifecycleController.class)
class SensorLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensorLifecycleOrchestrator sensorLifecycleOrchestrator;

    @MockBean
    private AsyncSensorLifecycleOrchestrator asyncSensorLifecycleOrchestrator;

    @Autowired
    private ObjectMapper objectMapper;

    private SensorResponse sensorResponse;
    private SensorRegistrationRequest sensorRegistrationRequest;
    private final String SENSOR_ID = "sensor-123";

    @BeforeEach
    void setUp() {
        sensorResponse = new SensorResponse();
        sensorResponse.setSensorId(SENSOR_ID);
        sensorResponse.setSensorType("Temperature and Humidity Sensor");
        sensorResponse.setLocation("Test Location");
        sensorResponse.setState(SensorState.DISCONNECTED);

        sensorRegistrationRequest = new SensorRegistrationRequest();
        sensorRegistrationRequest.setSensorId(SENSOR_ID);
        sensorRegistrationRequest.setSensorType(SensorType.TEMPERATURE_AND_HUMIDITY_SENSOR);
        sensorRegistrationRequest.setLocation("Test Location");
        sensorRegistrationRequest.setLatitude(40.7128);
        sensorRegistrationRequest.setLongitude(-74.0060);
    }

    @Test
    void registerSensor_whenSuccessful_shouldReturn201Created() throws Exception {
        when(sensorLifecycleOrchestrator.registerSensor(any(SensorRegistrationRequest.class)))
                .thenReturn(sensorResponse);

        mockMvc.perform(post("/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sensorId").value(SENSOR_ID))
                .andExpect(jsonPath("$.state").value("DISCONNECTED"));
    }

    @Test
    void registerSensor_whenSensorAlreadyExists_shouldReturn409Conflict() throws Exception {
        String errorMessage = "Sensor with ID '" + SENSOR_ID + "' already exists.";
        when(sensorLifecycleOrchestrator.registerSensor(any(SensorRegistrationRequest.class)))
                .thenThrow(new SensorAlreadyExistsException(errorMessage));

        mockMvc.perform(post("/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorRegistrationRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getSensorById_whenSensorExists_shouldReturn200OK() throws Exception {
        when(sensorLifecycleOrchestrator.getSensorById(anyString()))
                .thenReturn(sensorResponse);

        mockMvc.perform(get("/sensors/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorId").value(SENSOR_ID));
    }

    @Test
    void getSensorById_whenSensorNotFound_shouldReturn404NotFound() throws Exception {
        String errorMessage = "Sensor with ID '" + SENSOR_ID + "' not found.";
        when(sensorLifecycleOrchestrator.getSensorById(anyString()))
                .thenThrow(new SensorNotFoundException(errorMessage));

        mockMvc.perform(get("/sensors/{sensorId}", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void sensorHeartbeat_whenGivenValidRequest_shouldCallAsyncOrchestratorAndReturnAccepted() throws Exception {
        HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
        heartbeatRequest.setStatus(SensorState.CONNECTED);

        mockMvc.perform(post("/sensors/{sensorId}/heartbeat", SENSOR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(heartbeatRequest)))
                .andExpect(status().isAccepted());

        verify(asyncSensorLifecycleOrchestrator).processHeartbeatAsync(SENSOR_ID, SensorState.CONNECTED);
        verifyNoInteractions(sensorLifecycleOrchestrator);
    }
}