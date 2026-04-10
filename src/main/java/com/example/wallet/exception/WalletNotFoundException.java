package com.example.wallet.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    private static final String WALLET_NOT_FOUND = "Wallet not found with ID: ";

    public WalletNotFoundException(Long id) {
        super(WALLET_NOT_FOUND + id);
    }

    public WalletNotFoundException(UUID id) {
        super(WALLET_NOT_FOUND + id);
    }
}