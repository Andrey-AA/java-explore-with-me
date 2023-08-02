package ru.practicum.exception;

public class RequestConflictException extends RuntimeException {

    public RequestConflictException(String massage) {
        super(massage);
    }
}
