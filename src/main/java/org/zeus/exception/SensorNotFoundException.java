package org.zeus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a sensor with the specified ID cannot be found.
 * <p>
 * The @ResponseStatus annotation tells Spring to automatically return a
 * 404 Not Found HTTP status code whenever this exception is thrown from a controller.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException(String message) {
        super(message);
    }
}
