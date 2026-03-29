package com.example.wallet.model;

import java.util.List;

public record WalletPerformanceDTO(
        AssetPerformanceDTO bestPerforming,
        AssetPerformanceDTO worstPerforming,
        List<AssetPerformanceDTO> allPerformances) {
}
