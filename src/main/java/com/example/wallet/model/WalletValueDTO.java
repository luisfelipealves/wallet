package com.example.wallet.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WalletValueDTO(
                UUID walletId,
                BigDecimal totalValue,
                List<AssetDTO> assetValues) {
}
