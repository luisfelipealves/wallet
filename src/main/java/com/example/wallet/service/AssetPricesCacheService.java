package com.example.wallet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AssetPricesCacheService {

    BigDecimal getCurrentAssetPrice(String symbol);

    BigDecimal getHistoricalAssetPrice(String symbol, LocalDateTime date);
}
