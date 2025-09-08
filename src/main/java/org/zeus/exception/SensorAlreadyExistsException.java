package org.zeus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class SensorAlreadyExistsException extends RuntimeException {
    public SensorAlreadyExistsException(String message) {
        super(message);
    }
}
