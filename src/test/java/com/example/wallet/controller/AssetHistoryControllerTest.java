package com.example.wallet.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.wallet.model.AssetHistoryDTO;
import com.example.wallet.service.AssetHistoryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssetHistoryController Unit Tests")
class AssetHistoryControllerTest {

    @Mock
    private AssetHistoryService assetHistoryService;

    @InjectMocks
    private AssetHistoryController assetHistoryController;

    private List<AssetHistoryDTO> mockHistoryData;

    @BeforeEach
    void setUp() {
        mockHistoryData = new ArrayList<>();
        mockHistoryData.add(
                new AssetHistoryDTO(1L, "BTC", new BigDecimal("50000.00"), LocalDateTime.now().minusHours(1)));
        mockHistoryData.add(
                new AssetHistoryDTO(2L, "BTC", new BigDecimal("49500.00"), LocalDateTime.now().minusHours(2)));
    }

    @Test
    @DisplayName("should return asset history successfully")
    void testGetAssetHistorySuccess() {
        // Arrange
        String symbol = "BTC";
        int page = 0;
        int size = 10;
        Page<AssetHistoryDTO> expectedPage = new PageImpl<>(mockHistoryData, PageRequest.of(page, size), 2);

        when(assetHistoryService.getAssetHistory(symbol, page, size))
                .thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<AssetHistoryDTO>> response = assetHistoryController.getAssetHistory(symbol, page, size);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals("BTC", response.getBody().getContent().get(0).symbol());
        verify(assetHistoryService, times(1)).getAssetHistory(symbol, page, size);
    }

}
