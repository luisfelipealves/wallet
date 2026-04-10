package com.example.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.example.wallet.entity.AssetEntity;
import com.example.wallet.entity.WalletEntity;
import com.example.wallet.model.WalletPerformanceDTO;
import com.example.wallet.repository.AssetRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.AssetPricesCacheService;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletValuationServiceImpl - calculateWalletPerformance")
class WalletValuationServiceImplPerformanceTest {

        @Mock
        private AssetRepository assetRepository;

        @Mock
        private WalletRepository walletRepository;

        @Mock
        private AssetPricesCacheService assetPricesCacheService;

        @InjectMocks
        private WalletValuationServiceImpl walletValuationService;

        private LocalDateTime testDate;
        private UUID walletId;
        private WalletEntity mockWallet;

        @BeforeEach
        void setUp() {
                walletId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
                testDate = LocalDateTime.of(2024, 3, 27, 12, 0, 0);

                mockWallet = new WalletEntity();
                mockWallet.setId(1L);
                mockWallet.setUuid(walletId);

                when(walletRepository.findByUuid(walletId)).thenReturn(Optional.of(mockWallet));
        }

        @Test
        @DisplayName("Should calculate wallet performance with multiple assets")
        void testCalculateWalletPerformance_MultipleAssets_Success() {
                // Arrange
                AssetEntity btc = createAsset("BTC", new BigDecimal("40000"));
                AssetEntity eth = createAsset("ETH", new BigDecimal("2000"));
                AssetEntity ada = createAsset("ADA", new BigDecimal("0.8"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(btc, eth, ada));

                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", testDate))
                                .thenReturn(new BigDecimal("50000"));

                when(assetPricesCacheService.getHistoricalAssetPrice("ETH", testDate))
                                .thenReturn(new BigDecimal("1800"));

                when(assetPricesCacheService.getHistoricalAssetPrice("ADA", testDate))
                                .thenReturn(new BigDecimal("0.9"));

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertNotNull(result);
                assertEquals("BTC", result.bestPerforming().symbol());
                assertEquals(25.0, result.bestPerforming().performancePercentage(), 0.01);

                assertEquals("ETH", result.worstPerforming().symbol());
                assertEquals(-10.0, result.worstPerforming().performancePercentage(), 0.01);

                assertEquals(3, result.allPerformances().size());

                // Verify order is descending
                assertEquals(25.0, result.allPerformances().get(0).performancePercentage(), 0.01);
                assertEquals(12.5, result.allPerformances().get(1).performancePercentage(), 0.01);
                assertEquals(-10.0, result.allPerformances().get(2).performancePercentage(), 0.01);
        }

        @Test
        @DisplayName("Should identify best performer with highest positive performance")
        void testCalculateWalletPerformance_BestPerformer_Identified() {
                // Arrange
                AssetEntity asset1 = createAsset("ASSET1", new BigDecimal("100"));
                AssetEntity asset2 = createAsset("ASSET2", new BigDecimal("100"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(asset1, asset2));

                when(assetPricesCacheService.getHistoricalAssetPrice("ASSET1", testDate))
                                .thenReturn(new BigDecimal("200"));

                when(assetPricesCacheService.getHistoricalAssetPrice("ASSET2", testDate))
                                .thenReturn(new BigDecimal("150"));

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertEquals("ASSET1", result.bestPerforming().symbol());
                assertEquals(100.0, result.bestPerforming().performancePercentage(), 0.01);
        }

        @Test
        @DisplayName("Should identify worst performer with lowest negative performance")
        void testCalculateWalletPerformance_WorstPerformer_Identified() {
                // Arrange
                AssetEntity asset1 = createAsset("ASSET1", new BigDecimal("100"));
                AssetEntity asset2 = createAsset("ASSET2", new BigDecimal("100"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(asset1, asset2));

                when(assetPricesCacheService.getHistoricalAssetPrice("ASSET1", testDate))
                                .thenReturn(new BigDecimal("50"));

                when(assetPricesCacheService.getHistoricalAssetPrice("ASSET2", testDate))
                                .thenReturn(new BigDecimal("90"));

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertEquals("ASSET1", result.worstPerforming().symbol());
                assertEquals(-50.0, result.worstPerforming().performancePercentage(), 0.01);
        }

        @Test
        @DisplayName("Should handle single asset")
        void testCalculateWalletPerformance_SingleAsset() {
                // Arrange
                AssetEntity asset = createAsset("BTC", new BigDecimal("40000"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(asset));

                when(assetPricesCacheService.getHistoricalAssetPrice("BTC", testDate))
                                .thenReturn(new BigDecimal("50000"));

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertNotNull(result);
                assertEquals("BTC", result.bestPerforming().symbol());
                assertEquals("BTC", result.worstPerforming().symbol());
                assertEquals(1, result.allPerformances().size());
        }

        @Test
        @DisplayName("Should handle zero performance")
        void testCalculateWalletPerformance_ZeroPerformance() {
                // Arrange
                AssetEntity asset = createAsset("STABLE", new BigDecimal("100"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(asset));

                when(assetPricesCacheService.getHistoricalAssetPrice("STABLE", testDate))
                                .thenReturn(new BigDecimal("100"));

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertEquals(0.0, result.bestPerforming().performancePercentage(), 0.01);
                assertEquals(0.0, result.worstPerforming().performancePercentage(), 0.01);
        }

        @Test
        @DisplayName("Should handle missing historical price (defaults to zero)")
        void testCalculateWalletPerformance_MissingHistoricalPrice() {
                // Arrange
                AssetEntity asset = createAsset("UNKNOWN", new BigDecimal("100"));

                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of(asset));

                when(assetPricesCacheService.getHistoricalAssetPrice("UNKNOWN", testDate))
                                .thenReturn(BigDecimal.ZERO);

                // Act
                WalletPerformanceDTO result = walletValuationService.calculateWalletPerformance(walletId, testDate);

                // Assert
                assertNotNull(result);
                assertEquals(-100.0, result.bestPerforming().performancePercentage(), 0.01);
        }

        @Test
        @DisplayName("Should throw exception when no assets found")
        void testCalculateWalletPerformance_NoAssets_ThrowsException() {
                // Arrange
                when(assetRepository.findByWalletId(1L))
                                .thenReturn(List.of());

                // Act & Assert
                assertThrows(IllegalStateException.class,
                                () -> walletValuationService.calculateWalletPerformance(walletId, testDate));
        }

        // Helper methods
        private AssetEntity createAsset(String symbol, BigDecimal purchasePrice) {
                AssetEntity asset = new AssetEntity();
                asset.setId(System.nanoTime());
                asset.setSymbol(symbol);
                asset.setQuantity(BigDecimal.ONE);
                asset.setPurchasePrice(purchasePrice);
                asset.setPurchaseDate(testDate.minusDays(30)); // Set purchase date before test date
                return asset;
        }
}
