package com.example.wallet.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing a wallet with its assets")
public record WalletDTO(
                @Schema(description = "Unique wallet ID", example = "1") Long id,

                @Schema(description = "Wallet name", example = "My Wallet") String name,

                @Schema(description = "ID of the wallet owner user", example = "123") Long userId,

                @Schema(description = "List of assets in the wallet") List<AssetDTO> assets) {

}
