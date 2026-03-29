package com.example.wallet.service;

import java.math.BigDecimal;

import com.example.wallet.model.AssetDTO;

public interface AssetService {

    AssetDTO purchaseAsset(long walletId, String symbol, BigDecimal quantity);
}
