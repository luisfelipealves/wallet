package com.example.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import com.example.wallet.coincap.service.CoinCapService;
import com.example.wallet.config.PriceHistoryExecutorConfiguration;
import com.example.wallet.entity.AssetPriceHistoryEntity;
import com.example.wallet.mapper.AssetHistoryMapper;
import com.example.wallet.repository.AssetHistoryRepository;
import com.example.wallet.service.AssetHistoryService;

@SpringBootTest(classes = {
        PriceHistoryExecutorConfiguration.class,
        AssetHistoryServiceImpl.class,
        PriceHistoryExecutorFunctionalTest.TestConfig.class
})
@ActiveProfiles("test")
@DisplayName("PriceHistoryExecutor Functional Tests")
class PriceHistoryExecutorFunctionalTest {

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public CoinCapService coinCapServiceMock() {
            return mock(CoinCapService.class);
        }

        @Bean
        @Primary
        public AssetHistoryRepository assetHistoryRepositoryMock() {
            return mock(AssetHistoryRepository.class);
        }

        @Bean
        @Primary
        public AssetHistoryMapper assetHistoryMapperMock() {
            return mock(AssetHistoryMapper.class);
        }
    }

    @Autowired
    private AssetHistoryService assetHistoryService;

    @Autowired
    private CoinCapService coinCapService;

    @Autowired
    private AssetHistoryRepository assetHistoryRepository;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.reset(coinCapService, assetHistoryRepository);
    }

    @Test
    @DisplayName("should execute many tasks sequentially with 3 thread pool")
    void testExecutorWithHighVolumeRequests() throws InterruptedException {
        // Arrange
        int numberOfTasks = 100;
        CountDownLatch latch = new CountDownLatch(numberOfTasks);
        AtomicInteger successCount = new AtomicInteger(0);
        Set<String> uniqueThreadNames = Collections.synchronizedSet(new HashSet<>());

        when(coinCapService.getPriceBySymbol(anyString()))
                .thenAnswer(invocation -> {
                    String threadName = Thread.currentThread().getName();
                    uniqueThreadNames.add(threadName);
                    successCount.incrementAndGet();
                    return new BigDecimal("50000.00");
                });

        when(assetHistoryRepository.save(any(AssetPriceHistoryEntity.class)))
                .thenAnswer(invocation -> {
                    latch.countDown();
                    return invocation.getArgument(0);
                });

        // Act
        for (int i = 0; i < numberOfTasks; i++) {
            assetHistoryService.updatePriceAsync("ASSET_" + i);
        }

        // Wait for all tasks to complete
        boolean completed = latch.await(20, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "All " + numberOfTasks + " async tasks should complete within 20 seconds");
        assertEquals(numberOfTasks, successCount.get(),
                "All " + numberOfTasks + " tasks should have executed successfully");

        assertTrue(uniqueThreadNames.size() <= 3,
                "Should use at most 3 threads, but used: " + uniqueThreadNames.size() +
                        " Thread names: " + uniqueThreadNames);

        assertTrue(uniqueThreadNames.stream().allMatch(name -> name.startsWith("CoinCap-")),
                "All threads should be from the CoinCap executor pool");
    }

    @Test
    @DisplayName("should handle exceptions without blocking thread pool")
    void testExecutorHandlesExceptionsGracefully() throws InterruptedException {
        // Arrange
        List<String> symbols = List.of("BTC", "ETH", "ADA", "XRP");
        int totalTasks = symbols.size();

        // Track both successful and failed tasks
        CountDownLatch completionLatch = new CountDownLatch(totalTasks);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        when(coinCapService.getPriceBySymbol(anyString()))
                .thenAnswer(invocation -> {
                    String symbol = (String) invocation.getArguments()[0];
                    if ("ETH".equals(symbol) || "XRP".equals(symbol)) {
                        failureCount.incrementAndGet();
                        completionLatch.countDown();
                        throw new RuntimeException("API Error for " + symbol);
                    }
                    successCount.incrementAndGet();
                    return new BigDecimal("50000.00");
                });

        when(assetHistoryRepository.save(any(AssetPriceHistoryEntity.class)))
                .thenAnswer(invocation -> {
                    completionLatch.countDown();
                    return invocation.getArgument(0);
                });

        // Act - Call async tasks
        for (String symbol : symbols) {
            assetHistoryService.updatePriceAsync(symbol);
        }

        // Wait for all tasks to complete (both successful and failed)
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "All " + totalTasks + " tasks should complete within 10 seconds");
        assertEquals(2, successCount.get(), "2 tasks should succeed (BTC, ADA)");
        assertEquals(2, failureCount.get(), "2 tasks should fail (ETH, XRP - exceptions are caught gracefully)");
    }

}
