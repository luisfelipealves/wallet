package com.example.wallet.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
public class PriceHistoryExecutorConfiguration {

    @Value("${application.thread-pool.core-size:3}")
    private int corePoolSize;

    @Value("${application.thread-pool.max-size:3}")
    private int maxPoolSize;

    @Value("${application.thread-pool.queue-capacity:500}")
    private int queueCapacity;

    @Value("${application.thread-pool.thread-name-prefix:CoinCap-}")
    private String threadNamePrefix;

    @Bean
    public Executor priceHistoryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
