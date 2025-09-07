package org.zeus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an attempt is made to register a sensor
 * with an ID that already exists in the system.
 * <p>
 * The @ResponseStatus annotation tells Spring to automatically return a
 * 409 Conflict HTTP status code whenever this exception is thrown from a controller.
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class SensorAlreadyExistsException extends RuntimeException {
    public SensorAlreadyExistsException(String message) {
        super(message);
    }
}
