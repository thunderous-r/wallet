package com.example.wallet.exception;

public class ConcurrentOperationException extends RuntimeException {
    public ConcurrentOperationException(String message) {
        super(message);
    }

    public ConcurrentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 