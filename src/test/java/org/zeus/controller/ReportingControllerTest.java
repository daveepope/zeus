package org.zeus.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.zeus.exception.SensorNotFoundException;
import org.zeus.model.AggregatedMeasurementResponse;
import org.zeus.model.AggregatedMetric;
import org.zeus.model.AggregatedSensorResult;
import org.zeus.model.MetricType;
import org.zeus.model.StatisticType;
import org.zeus.service.report.ReportingOrchestrator;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportingController.class)
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingOrchestrator reportingOrchestrator;

    private AggregatedMeasurementResponse aggregatedResponse;
    private final String SENSOR_ID = "PLANET-EXPRESS-HQ-TEMP-HUMIDITY";

    @BeforeEach
    void setUp() {
        AggregatedMetric metric = new AggregatedMetric();
        metric.setMetricType(MetricType.TEMPERATURE);
        metric.setStatistic(StatisticType.AVERAGE);
        metric.setValue(22.5);

        AggregatedSensorResult result = new AggregatedSensorResult();
        result.setSensorId(SENSOR_ID);
        result.setAggregatedMetrics(Collections.singletonList(metric));

        aggregatedResponse = new AggregatedMeasurementResponse();
        aggregatedResponse.setResults(Collections.singletonList(result));
        aggregatedResponse.setQueryTimestamp(OffsetDateTime.now());
    }

    @Test
    void getAggregatedMeasurements_withValidParameters_shouldReturn200OK() throws Exception {
        when(reportingOrchestrator.getAggregatedMeasurements(any(), any(), any(), any(), any()))
                .thenReturn(aggregatedResponse);

        mockMvc.perform(get("/reports/measurements")
                        .param("sensorIds", SENSOR_ID)
                        .param("metricTypes", "TEMPERATURE")
                        .param("statistic", "average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].sensorId").value(SENSOR_ID))
                .andExpect(jsonPath("$.results[0].aggregatedMetrics[0].metricType").value("TEMPERATURE"))
                .andExpect(jsonPath("$.results[0].aggregatedMetrics[0].value").value(22.5));
    }

    @Test
    void getAggregatedMeasurements_whenMetricTypesAreMissing_shouldReturn400BadRequest() throws Exception {
        mockMvc.perform(get("/reports/measurements")
                        .param("sensorIds", SENSOR_ID)
                        .param("statistic", "average"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAggregatedMeasurements_whenStatisticIsMissing_shouldReturn400BadRequest() throws Exception {
        mockMvc.perform(get("/reports/measurements")
                        .param("sensorIds", SENSOR_ID)
                        .param("metricTypes", "TEMPERATURE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAggregatedMeasurements_whenSensorIsNotFound_shouldReturn404NotFound() throws Exception {
        String errorMessage = "Sensor with ID '" + SENSOR_ID + "' not found.";
        when(reportingOrchestrator.getAggregatedMeasurements(any(), any(), any(), any(), any()))
                .thenThrow(new SensorNotFoundException(errorMessage));

        mockMvc.perform(get("/reports/measurements")
                        .param("sensorIds", SENSOR_ID)
                        .param("metricTypes", "TEMPERATURE")
                        .param("statistic", "average"))
                .andExpect(status().isNotFound());
    }
}
