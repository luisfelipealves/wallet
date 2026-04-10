package com.example.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.wallet.entity.WalletEntity;
import com.example.wallet.model.AssetDTO;
import com.example.wallet.model.WalletValueDTO;
import com.example.wallet.repository.AssetRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.AssetPricesCacheService;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletValuationServiceImpl - getHistoricalWalletValue")
class WalletValuationServiceImplHistoricalTest {

        @Mock
        private WalletRepository walletRepository;

        @Mock
        private AssetRepository assetRepository;

        @Mock
        private AssetPricesCacheService assetPricesCacheService;

        @InjectMocks
        private WalletValuationServiceImpl walletValuationService;

        private WalletEntity mockWallet;
        private UUID walletUuid;
        private LocalDateTime queryDate;
        private LocalDateTime purchaseDate;

        @BeforeEach
        void setUp() {
                queryDate = LocalDateTime.of(2026, 1, 15, 10, 0, 0);
                purchaseDate = LocalDateTime.of(2025, 12, 1, 10, 0, 0);

                walletUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

                mockWallet = new WalletEntity();
                mockWallet.setId(1L);
                mockWallet.setUuid(walletUuid);

                when(walletRepository.findByUuid(walletUuid)).thenReturn(Optional.of(mockWallet));
        }

        @Test
        @DisplayName("Should return wallet value with historical prices for assets before query date")
        void testGetHistoricalWalletValue_Success() {
                // Arrange
                AssetDTO aggregatedAsset = new AssetDTO(
                                1L,
                                "BTC",
                                BigDecimal.valueOf(0.5),
                                BigDecimal.valueOf(42000),
                                purchaseDate,
                                1L);

                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, queryDate))
                                .thenReturn(List.of(aggregatedAsset));
                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", queryDate))
                                .thenReturn(BigDecimal.valueOf(42000));

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, queryDate);

                // Assert
                assertNotNull(result);
                assertEquals(walletUuid, result.walletId());
                assertEquals(BigDecimal.valueOf(21000).setScale(2), result.totalValue().setScale(2));
                assertEquals(1, result.assetValues().size());
                assertEquals("BTC", result.assetValues().get(0).symbol());
        }

        @Test
        @DisplayName("Should exclude assets purchased after query date")
        void testGetHistoricalWalletValue_ExcludeAssetsAfterDate() {
                // Arrange
                AssetDTO btcAsset = new AssetDTO(
                                1L,
                                "BTC",
                                BigDecimal.valueOf(0.5),
                                BigDecimal.valueOf(42000),
                                purchaseDate,
                                1L);

                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, queryDate))
                                .thenReturn(List.of(btcAsset));
                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", queryDate))
                                .thenReturn(BigDecimal.valueOf(42000));

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, queryDate);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.assetValues().size());
                assertEquals("BTC", result.assetValues().get(0).symbol());
                assertEquals(BigDecimal.valueOf(21000).setScale(1), result.totalValue().setScale(1));
        }

        @Test
        @DisplayName("Should return zero value when all assets purchased after query date")
        void testGetHistoricalWalletValue_AllAssetsAfterDate() {
                // Arrange
                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, queryDate))
                                .thenReturn(List.of());

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, queryDate);

                // Assert
                assertNotNull(result);
                assertEquals(walletUuid, result.walletId());
                assertEquals(BigDecimal.ZERO.setScale(1), result.totalValue().setScale(1));
                assertEquals(0, result.assetValues().size());
        }

        @Test
        @DisplayName("Should handle multiple assets with correct historical values")
        void testGetHistoricalWalletValue_MultipleAssets() {
                // Arrange
                AssetDTO btcAsset = new AssetDTO(
                                1L,
                                "BTC",
                                BigDecimal.valueOf(0.5),
                                BigDecimal.valueOf(42000),
                                purchaseDate,
                                1L);

                AssetDTO ethAsset = new AssetDTO(
                                2L,
                                "ETH",
                                BigDecimal.valueOf(5),
                                BigDecimal.valueOf(2500),
                                LocalDateTime.of(2025, 11, 1, 10, 0, 0),
                                1L);

                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, queryDate))
                                .thenReturn(List.of(btcAsset, ethAsset));
                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", queryDate))
                                .thenReturn(BigDecimal.valueOf(42000));
                when(assetPricesCacheService.getHistoricalAssetPrice("ETH", queryDate))
                                .thenReturn(BigDecimal.valueOf(2500));

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, queryDate);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.assetValues().size());
                // 0.5 * 42000 + 5 * 2500 = 21000 + 12500 = 33500
                assertEquals(BigDecimal.valueOf(33500).setScale(1), result.totalValue().setScale(1));
        }

        @Test
        @DisplayName("Should handle zero price when no historical data available")
        void testGetHistoricalWalletValue_NoHistoricalPrice() {
                // Arrange
                AssetDTO aggregatedAsset = new AssetDTO(
                                1L,
                                "BTC",
                                BigDecimal.valueOf(0.5),
                                BigDecimal.ZERO,
                                purchaseDate,
                                1L);

                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, queryDate))
                                .thenReturn(List.of(aggregatedAsset));
                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", queryDate))
                                .thenReturn(BigDecimal.ZERO);

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, queryDate);

                // Assert
                assertNotNull(result);
                assertEquals(walletUuid, result.walletId());
                assertEquals(BigDecimal.ZERO.setScale(1), result.totalValue().setScale(1));
                assertEquals(1, result.assetValues().size());
        }

        @Test
        @DisplayName("Should return wallet value when query date equals purchase date")
        void testGetHistoricalWalletValue_QueryDateEqualsPurchaseDate() {
                // Arrange
                LocalDateTime sameDate = LocalDateTime.of(2025, 12, 1, 10, 0, 0);
                AssetDTO aggregatedAsset = new AssetDTO(
                                1L,
                                "BTC",
                                BigDecimal.valueOf(0.5),
                                BigDecimal.valueOf(42000),
                                sameDate,
                                1L);

                when(assetRepository.findAggregatedAssetsByWalletIdAndDate(1L, sameDate))
                                .thenReturn(List.of(aggregatedAsset));
                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", sameDate))
                                .thenReturn(BigDecimal.valueOf(42000));

                // Act
                WalletValueDTO result = walletValuationService.getHistoricalWalletValue(walletUuid, sameDate);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.assetValues().size());
                assertEquals(BigDecimal.valueOf(21000).setScale(2), result.totalValue().setScale(2));
        }
}
