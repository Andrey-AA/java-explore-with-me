package ru.practicum.exception;

public class InvalidTimeParameterException extends RuntimeException {
    public InvalidTimeParameterException(String message) {
        super(message);
    }
}
