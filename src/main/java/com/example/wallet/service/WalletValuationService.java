package com.example.wallet.service;

import java.time.LocalDateTime;

import com.example.wallet.model.WalletPerformanceDTO;
import com.example.wallet.model.WalletValueDTO;

public interface WalletValuationService {

    WalletValueDTO getCurrentWalletValue(long walletId);

    WalletValueDTO getHistoricalWalletValue(long walletId, LocalDateTime date);

    WalletPerformanceDTO calculateWalletPerformance(long walletId, LocalDateTime date);
}
