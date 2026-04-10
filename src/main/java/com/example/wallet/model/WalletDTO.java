package com.example.wallet.model;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing a wallet with its assets")
public record WalletDTO(
        @Schema(description = "Unique wallet UUID", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,

        @Schema(description = "Wallet name", example = "My Wallet") String name,

        @Schema(description = "ID of the wallet owner user", example = "123") Long userId,

        @Schema(description = "List of assets in the wallet") List<AssetDTO> assets) {

}
