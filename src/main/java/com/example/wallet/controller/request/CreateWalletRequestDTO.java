package com.example.wallet.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateWalletRequestDTO(
        @Schema(description = "Wallet name", example = "My Wallet") @NotBlank(message = "Wallet name is required") @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters") String name,
        @Schema(description = "ID of the wallet owner user", example = "123") @NotNull(message = "User ID is required") @Positive(message = "User ID must be a positive number") Long userId) {

}
