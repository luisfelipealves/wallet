package com.example.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "price_history", indexes = {
        @Index(name = "idx_symbol_timestamp", columnList = "symbol, timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetPriceHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "price_usd", nullable = false, precision = 38, scale = 18)
    private BigDecimal priceUsd;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}