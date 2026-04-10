package com.example.wallet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.entity.AssetEntity;
import com.example.wallet.exception.InvalidAssetSymbolException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.mapper.AssetMapper;
import com.example.wallet.model.AssetDTO;
import com.example.wallet.repository.AssetRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.AssetHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements com.example.wallet.service.AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;
    private final WalletRepository walletRepository;
    private final AssetHistoryService assetHistoryService;

    @Override
    @Transactional
    public AssetDTO purchaseAsset(UUID walletId, String symbol, BigDecimal quantity) {

        BigDecimal assetPrice = assetHistoryService.recordCurrentPrice(symbol);
        if (assetPrice == null) {
            throw new InvalidAssetSymbolException("Invalid or not found asset symbol: " + symbol);
        }

        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setSymbol(symbol);
        assetEntity.setQuantity(quantity);
        assetEntity.setPurchasePrice(assetPrice);
        assetEntity.setWallet(walletRepository.findByUuid(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId)));
        assetEntity.setPurchaseDate(LocalDateTime.now());

        assetRepository.save(assetEntity);

        return assetMapper.toAssetDTO(assetEntity);
    }

}
