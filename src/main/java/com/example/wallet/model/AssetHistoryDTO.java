package com.example.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetHistoryDTO(Long id,
        String symbol,
        BigDecimal priceUsd,
        LocalDateTime timestamp) {
}