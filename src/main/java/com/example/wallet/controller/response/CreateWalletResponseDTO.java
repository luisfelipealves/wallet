package com.example.wallet.controller.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for wallet creation response")
public record CreateWalletResponseDTO(
                @Schema(description = "UUID of the created wallet", example = "550e8400-e29b-41d4-a716-446655440000") UUID uuid) {

}
