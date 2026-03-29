package com.example.wallet.service.impl;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.wallet.repository.AssetRepository;
import com.example.wallet.service.AssetHistoryService;
import com.example.wallet.service.AssetPriceSchedulerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AssetPriceSchedulerServiceImpl implements AssetPriceSchedulerService {

    private final AssetRepository assetRepository;
    private final AssetHistoryService assetHistoryService;

    @Override
    @Scope("application")
    @Scheduled(fixedRateString = "${application.scheduler.fixed-rate-ms:30000}")
    public void refreshAllPrices() {
        List<String> symbols = assetRepository.findAllUniqueSymbols();
        symbols.forEach(assetHistoryService::updatePriceAsync);
    }

}
