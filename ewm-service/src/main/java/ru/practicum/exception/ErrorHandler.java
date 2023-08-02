package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    /** 404*/
    public ApiError handleObjectNotFoundException(final ObjectNotFoundException e) {
        log.info(e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(String.valueOf(HttpStatus.NOT_FOUND));
        apiError.setReason(HttpStatus.NOT_FOUND.name());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /** 400*/
    public ApiError handleRequestNotValidException(final RequestNotValidException e) {
        log.info(e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(String.valueOf(HttpStatus.BAD_REQUEST));
        apiError.setReason(HttpStatus.BAD_REQUEST.name());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler(RequestConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    /** 409*/
    public ApiError handleRequestConflictException(final RequestConflictException e) {
        log.info(e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(String.valueOf(HttpStatus.CONFLICT));
        apiError.setReason(HttpStatus.CONFLICT.name());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    /** 409*/
    public ApiError handleSQLException(final SQLException e) {
        log.info(e.getMessage());
        ApiError apiError = new ApiError();
        apiError.setStatus(String.valueOf(HttpStatus.CONFLICT));
        apiError.setReason(HttpStatus.CONFLICT.name());
        apiError.setMessage(e.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }
}
