package com.example.wallet.model;

import java.math.BigDecimal;

public record AssetPerformanceDTO(
        String symbol,
        BigDecimal currentPrice,
        BigDecimal purchasePrice,
        Double performancePercentage) {
}