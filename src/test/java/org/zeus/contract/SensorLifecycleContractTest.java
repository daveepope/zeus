/*
package org.zeus.contract;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.zeus.model.SensorResponse;
import org.zeus.model.SensorState;
import org.zeus.service.sensorLifecycle.SensorLifecycleOrchestrator;

import static org.mockito.Mockito.when;

*/
/**
 * This is the Provider-side contract test.
 * It verifies that this API (the Provider) adheres to the contracts
 * published by its clients (the Consumers).
 *//*

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("TelemetryIngestionApi") // The name of our API (the Provider)
@PactFolder("pacts") // Tells Pact to look for contract files in the "pacts" directory
public class SensorLifecycleContractTest {

    @MockBean // We use @MockBean to control the service layer, just like in our controller tests
    private SensorLifecycleOrchestrator sensorLifecycleOrchestrator;

    @LocalServerPort
    private int port;

    */
/**
     * This method is executed by Pact BEFORE verifying an interaction.
     * Its job is to set up the target of the test (your API).
     *//*

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    */
/**
     * This is the test template that Pact runs. It will execute once for every
     * interaction defined in the contract files.
     *//*

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    */
/**
     * This method is executed by Pact BEFORE verifying an interaction that has
     * the state "a sensor with ID sensor-123 exists".
     * Its job is to set up the backend to be in that specific state.
     *//*

    @State("a sensor with ID sensor-123 exists")
    public void sensorExistsState() {
        SensorResponse sensorResponse = new SensorResponse();
        sensorResponse.setSensorId("sensor-123");
        sensorResponse.setSensorType("Temperature and Humidity Sensor");
        sensorResponse.setLocation("Test Location");
        sensorResponse.setState(SensorState.DISCONNECTED);

        when(sensorLifecycleOrchestrator.getSensorById("sensor-123")).thenReturn(sensorResponse);
    }

    */
/**
     * This method is executed for the state "a sensor with ID sensor-456 does not exist".
     * Here, we don't need to do anything, because by default our mock will return null.
     * This state handler is useful for clarity.
     *//*

    @State("a sensor with ID sensor-456 does not exist")
    public void sensorDoesNotExistState() {
        // No setup needed, the mock will return null for any sensor ID that doesn't exist
    }
}*/
