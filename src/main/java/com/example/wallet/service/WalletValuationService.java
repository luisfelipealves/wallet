package com.example.wallet.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.wallet.model.WalletPerformanceDTO;
import com.example.wallet.model.WalletValueDTO;

public interface WalletValuationService {

    WalletValueDTO getCurrentWalletValue(UUID walletId);

    WalletValueDTO getHistoricalWalletValue(UUID walletId, LocalDateTime date);

    WalletPerformanceDTO calculateWalletPerformance(UUID walletId, LocalDateTime date);
}
