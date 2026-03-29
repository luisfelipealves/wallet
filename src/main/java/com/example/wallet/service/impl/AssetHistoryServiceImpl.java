package com.example.wallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.wallet.coincap.service.CoinCapService;
import com.example.wallet.entity.AssetPriceHistoryEntity;
import com.example.wallet.exception.AssetHistoryNotFoundException;
import com.example.wallet.exception.InvalidPaginationException;
import com.example.wallet.mapper.AssetHistoryMapper;
import com.example.wallet.model.AssetHistoryDTO;
import com.example.wallet.repository.AssetHistoryRepository;
import com.example.wallet.service.AssetHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetHistoryServiceImpl implements AssetHistoryService {

    @Value("${application.pagination.max-size:100}")
    private int maxPageSize;

    private final AssetHistoryRepository assetHistoryRepository;
    private final AssetHistoryMapper assetHistoryMapper;
    private final CoinCapService coinCapService;

    @Override
    public Page<AssetHistoryDTO> getAssetHistory(String symbol, int page, int size) {
        if (page < 0) {
            throw new InvalidPaginationException("Page cannot be negative. Received: " + page);
        }
        if (size <= 0 || size > maxPageSize) {
            throw new InvalidPaginationException(
                    "Page size must be between 1 and " + maxPageSize + ". Received: " + size);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AssetHistoryDTO> result = assetHistoryRepository.findBySymbol(symbol, pageable)
                .map(assetHistoryMapper::toAssetHistoryDTO);

        if (result.getTotalElements() == 0) {
            throw new AssetHistoryNotFoundException(symbol);
        }

        if (result.isEmpty()) {
            int lastPage = result.getTotalPages() - 1;
            pageable = PageRequest.of(lastPage, size, Sort.by("timestamp").descending());
            result = assetHistoryRepository.findBySymbol(symbol, pageable)
                    .map(assetHistoryMapper::toAssetHistoryDTO);
        }

        return result;
    }

    @Override
    public BigDecimal recordCurrentPrice(String symbol) {
        BigDecimal latestPrice = coinCapService.getPriceBySymbol(symbol);

        if (latestPrice != null) {
            AssetPriceHistoryEntity history = AssetPriceHistoryEntity.builder()
                    .symbol(symbol)
                    .priceUsd(latestPrice)
                    .timestamp(LocalDateTime.now())
                    .build();
            assetHistoryRepository.save(history);
        }

        return latestPrice;
    }

    @Override
    @Async("priceHistoryExecutor")
    public CompletableFuture<BigDecimal> updatePriceAsync(String symbol) {
        try {
            return CompletableFuture.completedFuture(recordCurrentPrice(symbol));
        } catch (Exception e) {
            log.error("Error updating price for " + symbol + ": " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

}
