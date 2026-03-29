package com.example.wallet.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;

import com.example.wallet.model.AssetHistoryDTO;

public interface AssetHistoryService {

    Page<AssetHistoryDTO> getAssetHistory(String symbol, int page, int size);

    BigDecimal recordCurrentPrice(String symbol);

    CompletableFuture<BigDecimal> updatePriceAsync(String symbol);
}
