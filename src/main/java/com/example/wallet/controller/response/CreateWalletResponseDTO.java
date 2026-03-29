package com.example.wallet.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for wallet creation response")
public record CreateWalletResponseDTO(
        @Schema(description = "ID of the created wallet", example = "1") Long id) {

}
