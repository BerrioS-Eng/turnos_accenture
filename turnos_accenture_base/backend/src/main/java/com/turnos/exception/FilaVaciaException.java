package com.turnos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FilaVaciaException extends RuntimeException {
    public FilaVaciaException(String message) {
        super(message);
    }
}
