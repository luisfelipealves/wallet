package com.example.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.wallet.coincap.service.CoinCapService;
import com.example.wallet.entity.AssetPriceHistoryEntity;
import com.example.wallet.exception.AssetHistoryNotFoundException;
import com.example.wallet.mapper.AssetHistoryMapper;
import com.example.wallet.model.AssetHistoryDTO;
import com.example.wallet.repository.AssetHistoryRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AssetHistoryServiceImpl Unit Tests")
class AssetHistoryServiceImplTest {

    @Mock
    private AssetHistoryRepository assetHistoryRepository;

    @Mock
    private AssetHistoryMapper assetHistoryMapper;

    @Mock
    private CoinCapService coinCapService;

    @InjectMocks
    private AssetHistoryServiceImpl assetHistoryService;

    private AssetPriceHistoryEntity historyEntity1;
    private AssetPriceHistoryEntity historyEntity2;
    private AssetHistoryDTO historyDTO1;
    private AssetHistoryDTO historyDTO2;
    private String testSymbol;

    @BeforeEach
    void setUp() {
        testSymbol = "BTC";

        // Inject @Value for maxPageSize using ReflectionTestUtils
        ReflectionTestUtils.setField(assetHistoryService, "maxPageSize", 100);

        // Setup first history entity
        historyEntity1 = new AssetPriceHistoryEntity();
        historyEntity1.setId(1L);
        historyEntity1.setSymbol(testSymbol);
        historyEntity1.setPriceUsd(new BigDecimal("50000.00"));
        historyEntity1.setTimestamp(LocalDateTime.now().minusHours(1));

        // Setup second history entity
        historyEntity2 = new AssetPriceHistoryEntity();
        historyEntity2.setId(2L);
        historyEntity2.setSymbol(testSymbol);
        historyEntity2.setPriceUsd(new BigDecimal("49500.00"));
        historyEntity2.setTimestamp(LocalDateTime.now().minusHours(2));

        // Setup DTOs
        historyDTO1 = new AssetHistoryDTO(1L, testSymbol, new BigDecimal("50000.00"), historyEntity1.getTimestamp());
        historyDTO2 = new AssetHistoryDTO(2L, testSymbol, new BigDecimal("49500.00"), historyEntity2.getTimestamp());
    }

    @Test
    @DisplayName("Should retrieve asset history successfully with pagination")
    void testGetAssetHistorySuccess() {
        // Arrange
        int page = 0;
        int size = 10;
        List<AssetPriceHistoryEntity> entities = List.of(historyEntity1, historyEntity2);
        Page<AssetPriceHistoryEntity> expectedPage = new PageImpl<>(entities, PageRequest.of(page, size), 2);

        when(assetHistoryRepository.findBySymbol(eq(testSymbol), any(Pageable.class)))
                .thenReturn(expectedPage);
        when(assetHistoryMapper.toAssetHistoryDTO(historyEntity1)).thenReturn(historyDTO1);
        when(assetHistoryMapper.toAssetHistoryDTO(historyEntity2)).thenReturn(historyDTO2);

        // Act
        Page<AssetHistoryDTO> result = assetHistoryService.getAssetHistory(testSymbol, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(historyDTO1.id(), result.getContent().get(0).id());
        assertEquals(historyDTO2.id(), result.getContent().get(1).id());
        verify(assetHistoryRepository, times(1)).findBySymbol(eq(testSymbol), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no history found for symbol")
    void testGetAssetHistoryEmpty() {
        // Arrange
        int page = 0;
        int size = 10;
        Page<AssetPriceHistoryEntity> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(page, size), 0);

        when(assetHistoryRepository.findBySymbol(eq(testSymbol), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        assertThrows(AssetHistoryNotFoundException.class, () -> {
            assetHistoryService.getAssetHistory(testSymbol, page, size);
        });

        verify(assetHistoryRepository, times(1)).findBySymbol(eq(testSymbol), any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle second page pagination correctly")
    void testGetAssetHistorySecondPage() {
        // Arrange
        int page = 1;
        int size = 1;
        List<AssetPriceHistoryEntity> entities = List.of(historyEntity2);
        Page<AssetPriceHistoryEntity> expectedPage = new PageImpl<>(entities, PageRequest.of(page, size), 2);

        when(assetHistoryRepository.findBySymbol(eq(testSymbol), any(Pageable.class)))
                .thenReturn(expectedPage);
        when(assetHistoryMapper.toAssetHistoryDTO(historyEntity2)).thenReturn(historyDTO2);

        // Act
        Page<AssetHistoryDTO> result = assetHistoryService.getAssetHistory(testSymbol, page, size);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(historyDTO2.id(), result.getContent().get(0).id());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getTotalElements());
        verify(assetHistoryRepository, times(1)).findBySymbol(eq(testSymbol), any(Pageable.class));
    }

    // ===== recordCurrentPrice Tests =====

    @Test
    @DisplayName("Should save and return price when retrieved successfully")
    void testRecordCurrentPriceSuccess() {
        // Arrange
        String symbol = "BTC";
        BigDecimal mockPrice = new BigDecimal("45000.50");

        when(coinCapService.getPriceBySymbol(symbol)).thenReturn(mockPrice);

        // Act
        BigDecimal result = assetHistoryService.recordCurrentPrice(symbol);

        // Assert
        assertEquals(mockPrice, result);
        verify(coinCapService, times(1)).getPriceBySymbol(symbol);
        verify(assetHistoryRepository, times(1)).save(any(AssetPriceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should not save and return null when price is null")
    void testRecordCurrentPriceNullPrice() {
        // Arrange
        String symbol = "ETH";

        when(coinCapService.getPriceBySymbol(symbol)).thenReturn(null);

        // Act
        BigDecimal result = assetHistoryService.recordCurrentPrice(symbol);

        // Assert
        assertNull(result);
        verify(coinCapService, times(1)).getPriceBySymbol(symbol);
        verify(assetHistoryRepository, never()).save(any(AssetPriceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should propagate exception to caller")
    void testRecordCurrentPricePropagatesException() {
        // Arrange
        String symbol = "DOGE";

        when(coinCapService.getPriceBySymbol(symbol)).thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> assetHistoryService.recordCurrentPrice(symbol));
        verify(assetHistoryRepository, never()).save(any(AssetPriceHistoryEntity.class));
    }

    // ===== updatePriceAsync Tests =====

    @Test
    @DisplayName("Should delegate to recordCurrentPrice and return its value")
    void testUpdatePriceAsyncSuccess() {
        // Arrange
        String symbol = "BTC";
        BigDecimal mockPrice = new BigDecimal("45000.50");

        when(coinCapService.getPriceBySymbol(symbol)).thenReturn(mockPrice);

        // Act
        CompletableFuture<BigDecimal> result = assetHistoryService.updatePriceAsync(symbol);

        // Assert
        assertEquals(mockPrice, result.join());
        verify(assetHistoryRepository, times(1)).save(any(AssetPriceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should return null future when recordCurrentPrice returns null")
    void testUpdatePriceAsyncNullPrice() {
        // Arrange
        String symbol = "ETH";

        when(coinCapService.getPriceBySymbol(symbol)).thenReturn(null);

        // Act
        CompletableFuture<BigDecimal> result = assetHistoryService.updatePriceAsync(symbol);

        // Assert
        assertNull(result.join());
        verify(assetHistoryRepository, never()).save(any(AssetPriceHistoryEntity.class));
    }

    @Test
    @DisplayName("Should handle exception from recordCurrentPrice without throwing")
    void testUpdatePriceAsyncExceptionHandling() {
        // Arrange
        String symbol = "DOGE";

        when(coinCapService.getPriceBySymbol(symbol)).thenThrow(new RuntimeException("API Error"));

        // Act & Assert - should not throw
        CompletableFuture<BigDecimal> result = assetHistoryService.updatePriceAsync(symbol);

        // Assert exception was handled and null returned
        assertNull(result.join());
        verify(assetHistoryRepository, never()).save(any(AssetPriceHistoryEntity.class));
    }
}
