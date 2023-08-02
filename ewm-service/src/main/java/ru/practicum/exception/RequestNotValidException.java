package ru.practicum.exception;

public class RequestNotValidException extends RuntimeException {

    public RequestNotValidException(String massage) {
        super(massage);
    }
}
