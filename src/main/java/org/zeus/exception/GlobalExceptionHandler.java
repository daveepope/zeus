package org.zeus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zeus.model.Error;

/**
 * A centralized exception handler for the entire application.
 * The @ControllerAdvice annotation allows this class to intercept exceptions
 * thrown from any @RestController.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles cases where a client provides an invalid argument, such as a non-existent
     * Sensor Type. This will result in a 400 Bad Request response.
     *
     * @param ex The exception that was thrown.
     * @return A ResponseEntity containing a standardized Error object.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Error> handleIllegalArgumentException(IllegalArgumentException ex) {
        Error error = new Error();
        error.setCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where a sensor with the given ID already exists.
     * This will result in a 409 Conflict response.
     *
     * @param ex The exception that was thrown.
     * @return A ResponseEntity containing a standardized Error object.
     */
    @ExceptionHandler(SensorAlreadyExistsException.class)
    public ResponseEntity<Error> handleSensorAlreadyExistsException(SensorAlreadyExistsException ex) {
        Error error = new Error();
        error.setCode(HttpStatus.CONFLICT.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
