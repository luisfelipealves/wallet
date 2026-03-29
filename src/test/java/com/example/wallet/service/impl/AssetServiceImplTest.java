package com.example.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.wallet.entity.AssetEntity;
import com.example.wallet.entity.WalletEntity;
import com.example.wallet.mapper.AssetMapper;
import com.example.wallet.model.AssetDTO;
import com.example.wallet.repository.AssetRepository;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.AssetHistoryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetServiceImpl Unit Tests")
class AssetServiceImplTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetMapper assetMapper;

    @Mock
    private AssetHistoryService assetHistoryService;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private AssetServiceImpl assetService;

    private WalletEntity walletEntity;
    private AssetEntity savedAssetEntity;
    private AssetDTO expectedAssetDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        walletEntity = new WalletEntity();
        walletEntity.setId(1L);
        walletEntity.setName("Test Wallet");
        walletEntity.setUserId(100L);

        savedAssetEntity = new AssetEntity();
        savedAssetEntity.setId(1L);
        savedAssetEntity.setSymbol("BTC");
        savedAssetEntity.setQuantity(new BigDecimal("0.5"));
        savedAssetEntity.setPurchasePrice(new BigDecimal("45000.00"));
        savedAssetEntity.setPurchaseDate(LocalDateTime.now());
        savedAssetEntity.setWallet(walletEntity);

        expectedAssetDTO = new AssetDTO(
                1L,
                "BTC",
                new BigDecimal("0.5"),
                new BigDecimal("45000.00"),
                savedAssetEntity.getPurchaseDate(),
                1L);
    }

    @Test
    @DisplayName("Should purchase asset successfully and return AssetDTO")
    void testPurchaseAssetSuccess() {
        // Arrange
        long walletId = 1L;
        String symbol = "BTC";
        BigDecimal quantity = new BigDecimal("0.5");
        BigDecimal price = new BigDecimal("45000.00");

        when(assetHistoryService.recordCurrentPrice(symbol)).thenReturn(price);
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(walletEntity));
        when(assetRepository.save(any(AssetEntity.class))).thenReturn(savedAssetEntity);
        when(assetMapper.toAssetDTO(any(AssetEntity.class))).thenReturn(expectedAssetDTO);

        // Act
        AssetDTO result = assetService.purchaseAsset(walletId, symbol, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAssetDTO.id(), result.id());
        assertEquals(symbol, result.symbol());
        assertEquals(quantity, result.quantity());
        assertEquals(price, result.value());
        assertEquals(walletId, result.walletId());

        // Verify interactions
        verify(assetHistoryService, times(1)).recordCurrentPrice(symbol);
        verify(walletRepository, times(1)).findById(walletId);
        verify(assetRepository, times(1)).save(any(AssetEntity.class));
        verify(assetMapper, times(1)).toAssetDTO(any(AssetEntity.class));
    }

}
