package com.example.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.wallet.model.AssetDTO;

public interface AssetService {

    AssetDTO purchaseAsset(UUID walletId, String symbol, BigDecimal quantity);
}
