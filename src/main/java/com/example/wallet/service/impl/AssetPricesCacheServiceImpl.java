package com.example.wallet.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.wallet.entity.AssetPriceHistoryEntity;
import com.example.wallet.repository.AssetHistoryRepository;
import com.example.wallet.service.AssetPricesCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetPricesCacheServiceImpl implements AssetPricesCacheService {

    private final AssetHistoryRepository assetHistoryRepository;

    @Override
    @Cacheable(value = "assetPrices", key = "#symbol + ':' + T(java.time.LocalDate).now()", unless = "#result == null")
    public BigDecimal getCurrentAssetPrice(String symbol) {
        return assetHistoryRepository.findFirstBySymbolOrderByTimestampDesc(symbol)
                .map(AssetPriceHistoryEntity::getPriceUsd)
                .orElse(BigDecimal.ZERO)
                .setScale(18, RoundingMode.HALF_UP);
    }

    @Override
    @Cacheable(value = "assetPrices", key = "#symbol + ':' + T(java.time.LocalDate).from(#date.atZone(T(java.time.ZoneId).systemDefault()))", unless = "#result == null")
    public BigDecimal getHistoricalAssetPrice(String symbol, LocalDateTime date) {
        return assetHistoryRepository
                .findFirstBySymbolAndTimestampLessThanEqualOrderByTimestampDesc(symbol, date)
                .map(AssetPriceHistoryEntity::getPriceUsd)
                .orElse(BigDecimal.ZERO)
                .setScale(18, RoundingMode.HALF_UP);
    }
}
