package com.example.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset", indexes = {
        @Index(name = "idx_asset_wallet_date", columnList = "wallet_id, purchase_date"),
        @Index(name = "idx_asset_wallet_symbol_price_date", columnList = "wallet_id, symbol, purchase_price, purchase_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal quantity;

    @Column(name = "purchase_price", precision = 38, scale = 18)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private WalletEntity wallet;
}