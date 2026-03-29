package com.example.wallet.exception;

public class InvalidPaginationException extends IllegalArgumentException {
    public InvalidPaginationException(String message) {
        super(message);
    }
}
