package com.example.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing an asset in the wallet")
public record AssetDTO(
        @Schema(description = "Unique asset ID", example = "1") Long id,
        @Schema(description = "Asset symbol", example = "BTC") String symbol,
        @Schema(description = "Asset quantity", example = "0.5") BigDecimal quantity,
        @JsonInclude(Include.NON_NULL) @Schema(description = "Asset value", example = "45000.00") BigDecimal value,
        @JsonInclude(Include.NON_NULL) @Schema(description = "Purchase date and time", example = "2026-03-26T10:30:00") LocalDateTime purchaseDate,
        @Schema(description = "ID of the wallet the asset belongs to", example = "1") long walletId) {

    public AssetDTO(Long id, String symbol, BigDecimal quantity, long walletId) {
        this(id, symbol, quantity, null, null, walletId);
    }

    public AssetDTO(Long id, String symbol, LocalDateTime purchaseDate, BigDecimal quantity, long walletId) {
        this(id, symbol, quantity, null, purchaseDate, walletId);
    }
}