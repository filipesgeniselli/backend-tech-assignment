package com.filipegeniselli.backendtechassignment.exception;

import java.io.Serializable;

public class ConflictException extends RuntimeException implements Serializable {

    public static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
