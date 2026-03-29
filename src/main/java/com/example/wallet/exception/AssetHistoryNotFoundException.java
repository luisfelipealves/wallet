package com.example.wallet.exception;

public class AssetHistoryNotFoundException extends RuntimeException {

    private static final String ASSET_NOT_FOUND = "No price history found for symbol: ";

    public AssetHistoryNotFoundException(String symbol) {
        super(ASSET_NOT_FOUND + symbol);
    }
}
