package com.example.wallet.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.wallet.repository.AssetRepository;
import com.example.wallet.service.AssetHistoryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetPriceSchedulerServiceImpl Unit Tests")
class AssetPriceSchedulerServiceImplTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetHistoryService assetHistoryService;

    @InjectMocks
    private AssetPriceSchedulerServiceImpl assetPriceSchedulerService;

    @Test
    @DisplayName("should refresh prices for empty symbol list")
    void testRefreshAllPricesWithEmptyList() {
        when(assetRepository.findAllUniqueSymbols()).thenReturn(Collections.emptyList());

        assetPriceSchedulerService.refreshAllPrices();

        verify(assetRepository, times(1)).findAllUniqueSymbols();
        verify(assetHistoryService, times(0)).updatePriceAsync(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("should refresh prices for single symbol")
    void testRefreshAllPricesWithSingleSymbol() {
        List<String> symbols = List.of("BTC");
        when(assetRepository.findAllUniqueSymbols()).thenReturn(symbols);

        assetPriceSchedulerService.refreshAllPrices();

        verify(assetRepository, times(1)).findAllUniqueSymbols();
        verify(assetHistoryService, times(1)).updatePriceAsync("BTC");
    }

}
