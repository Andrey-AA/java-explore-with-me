package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidTimeParameterException(final InvalidTimeParameterException exception) {
        log.warn("Error! InvalidTimeParameterException, server status: '{}' text message: '{}'",
                HttpStatus.BAD_REQUEST, exception.getMessage());
        return Map.of("Error", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidDateFormatException(final InvalidDateFormatException exception) {
        log.warn("Error! InvalidDateFormatException, server status: '{}' text message: '{}'",
                HttpStatus.BAD_REQUEST, exception.getMessage());
        return Map.of("Error", exception.getMessage());
    }
}
