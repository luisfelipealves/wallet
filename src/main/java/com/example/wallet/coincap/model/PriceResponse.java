package com.example.wallet.coincap.model;

import java.util.List;

public record PriceResponse(
        long timestamp,
        List<String> data) {
}
