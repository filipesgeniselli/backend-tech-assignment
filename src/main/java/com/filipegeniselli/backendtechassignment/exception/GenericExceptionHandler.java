package com.filipegeniselli.backendtechassignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GenericExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException exception, WebRequest request) {
        logger.error("Failed to find requested object", exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(HttpStatus.NOT_FOUND.value(), exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorMessage> handleConflictException(ConflictException exception, WebRequest request) {
        logger.error("Invalid operation", exception);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorMessage(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleBadRequestException(BadRequestException exception, WebRequest request) {
        logger.error("Invalid request", exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(HttpStatus.BAD_REQUEST.value(), exception.getMessage()));
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleThrowable(Throwable exception, WebRequest request) {
        logger.error("Unhandled exception: ", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred, we're working to fix the issue, please try again later."));
    }

}
