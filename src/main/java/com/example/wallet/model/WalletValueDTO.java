package com.example.wallet.model;

import java.math.BigDecimal;
import java.util.List;

public record WalletValueDTO(
        Long walletId,
        BigDecimal totalValue,
        List<AssetDTO> assetValues) {
}
