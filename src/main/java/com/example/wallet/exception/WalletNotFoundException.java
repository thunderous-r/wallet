package com.example.wallet.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }

    public WalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 