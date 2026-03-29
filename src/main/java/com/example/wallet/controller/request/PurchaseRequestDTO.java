package com.example.wallet.controller.request;

import java.math.BigDecimal;

import com.example.wallet.validation.ValidAssetSymbol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequestDTO(
        @Schema(description = "Asset symbol (3-4 uppercase characters)", example = "BTC") @NotBlank(message = "Asset symbol is required") @ValidAssetSymbol String symbol,
        @Schema(description = "Quantity to purchase", example = "0.5") @NotNull(message = "Quantity is required") @DecimalMin(value = "0.001", message = "Minimum quantity is 0.001") @DecimalMax(value = "1000000", message = "Maximum quantity is 1000000") BigDecimal quantity) {

}
