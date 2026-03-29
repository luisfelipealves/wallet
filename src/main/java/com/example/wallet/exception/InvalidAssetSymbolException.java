package com.example.wallet.exception;

public class InvalidAssetSymbolException extends RuntimeException {

    public InvalidAssetSymbolException(String message) {
        super(message);
    }

    public InvalidAssetSymbolException(String message, Throwable cause) {
        super(message, cause);
    }
}
