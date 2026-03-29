package com.example.wallet.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.exception.WalletNotFoundException; // Custom exception
import com.example.wallet.model.*;
import com.example.wallet.repository.AssetRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.AssetPricesCacheService;
import com.example.wallet.service.WalletValuationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletValuationServiceImpl implements WalletValuationService {

        private static final int PRICE_PRECISION_SCALE = 18;
        private static final int CALCULATION_SCALE = 4;
        private static final BigDecimal PERCENTAGE_MULTIPLIER = BigDecimal.valueOf(100);
        private static final double ZERO_PERFORMANCE = 0.0;

        private final AssetRepository assetRepository;
        private final WalletRepository walletRepository;
        private final AssetPricesCacheService priceService;

        @Override
        public WalletValueDTO getCurrentWalletValue(long walletId) {
                validateWallet(walletId);

                var assets = assetRepository.findAggregatedAssetsByWalletId(walletId).stream()
                                .map(agg -> toAssetDTO(agg, walletId, priceService.getCurrentAssetPrice(agg.symbol())))
                                .toList();

                return new WalletValueDTO(walletId, calculateTotalValue(assets), assets);
        }

        @Override
        public WalletValueDTO getHistoricalWalletValue(long walletId, LocalDateTime date) {
                validateWallet(walletId);

                var assets = assetRepository.findAggregatedAssetsByWalletIdAndDate(walletId, date).stream()
                                .map(agg -> toAssetDTO(agg, walletId,
                                                priceService.getHistoricalAssetPrice(agg.symbol(), date)))
                                .toList();

                return new WalletValueDTO(walletId, calculateTotalValue(assets), assets);
        }

        @Override
        public WalletPerformanceDTO calculateWalletPerformance(long walletId, LocalDateTime date) {
                validateWallet(walletId);

                var performances = assetRepository.findByWalletId(walletId).stream()
                                .filter(asset -> !asset.getPurchaseDate().isAfter(date))
                                .map(asset -> createPerformanceDTO(asset, date))
                                .sorted(Comparator.comparing(AssetPerformanceDTO::performancePercentage).reversed())
                                .toList();

                if (performances.isEmpty()) {
                        throw new IllegalStateException("No assets found for performance calculation on " + date);
                }

                return new WalletPerformanceDTO(
                                performances.get(0),
                                performances.get(performances.size() - 1),
                                performances);
        }

        private void validateWallet(long walletId) {
                if (!walletRepository.existsById(walletId)) {
                        throw new WalletNotFoundException(walletId);
                }
        }

        private AssetDTO toAssetDTO(AssetDTO agg, long walletId, BigDecimal price) {
                return new AssetDTO(agg.id(), agg.symbol(), agg.quantity(), price, null, walletId);
        }

        private AssetPerformanceDTO createPerformanceDTO(com.example.wallet.entity.AssetEntity asset,
                        LocalDateTime date) {
                BigDecimal currentPrice = priceService.getHistoricalAssetPrice(asset.getSymbol(), date);
                BigDecimal purchasePrice = formatPrice(asset.getPurchasePrice());

                return new AssetPerformanceDTO(
                                asset.getSymbol(),
                                currentPrice,
                                purchasePrice,
                                calculateGrowthPercentage(purchasePrice, currentPrice));
        }

        private BigDecimal formatPrice(BigDecimal price) {
                return price.setScale(PRICE_PRECISION_SCALE, RoundingMode.HALF_UP);
        }

        private double calculateGrowthPercentage(BigDecimal initial, BigDecimal target) {
                if (isZeroOrNull(initial)) {
                        return ZERO_PERFORMANCE;
                }

                return target.subtract(initial)
                                .divide(initial, CALCULATION_SCALE, RoundingMode.HALF_UP)
                                .multiply(PERCENTAGE_MULTIPLIER)
                                .doubleValue();
        }

        private boolean isZeroOrNull(BigDecimal value) {
                return value == null || value.compareTo(BigDecimal.ZERO) == 0;
        }

        private BigDecimal calculateTotalValue(List<AssetDTO> assets) {
                return assets.stream()
                                .map(a -> a.quantity().multiply(a.value()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}