package com.example.wallet.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.controller.request.CreateWalletRequestDTO;
import com.example.wallet.controller.request.PurchaseRequestDTO;
import com.example.wallet.controller.response.CreateWalletResponseDTO;
import com.example.wallet.model.AssetDTO;
import com.example.wallet.model.WalletDTO;
import com.example.wallet.model.WalletPerformanceDTO;
import com.example.wallet.model.WalletValueDTO;
import com.example.wallet.service.AssetService;
import com.example.wallet.service.WalletService;
import com.example.wallet.service.WalletValuationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Operations related to the asset wallet")
public class WalletController {

        private final WalletService walletService;
        private final AssetService assetService;
        private final WalletValuationService walletValuationService;

        @PostMapping
        @Operation(summary = "Create a new wallet", description = "Creates a new empty wallet for the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Wallet created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateWalletResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<CreateWalletResponseDTO> createWallet(
                        @RequestBody @Valid CreateWalletRequestDTO request) {

                java.util.UUID walletUuid = walletService.createWallet(request.name(), request.userId());
                return ResponseEntity.status(HttpStatus.CREATED).body(new CreateWalletResponseDTO(walletUuid));
        }

        @GetMapping("/{walletId}")
        @Operation(summary = "Retrieve a wallet", description = "Returns the wallet data and its assets with purchase date (without current values)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Wallet retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Wallet not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<WalletDTO> getWallet(
                        @Parameter(description = "Wallet ID", required = true) @PathVariable Long walletId) {

                WalletDTO wallet = walletService.getWallet(walletId);
                return ResponseEntity.ok(wallet);
        }

        @PostMapping("/{walletId}/assets/purchase")
        @Operation(summary = "Purchase an asset", description = "Adds a new asset to the user's wallet")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Asset purchased successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Wallet not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<AssetDTO> addAssetToWallet(
                        @Parameter(description = "Wallet ID", required = true) @PathVariable Long walletId,
                        @RequestBody @Valid PurchaseRequestDTO request) { // Using RequestBody is better than
                                                                          // RequestParam

                AssetDTO asset = assetService.purchaseAsset(walletId, request.symbol(), request.quantity());
                return ResponseEntity.status(HttpStatus.CREATED).body(asset);
        }

        @GetMapping("/{walletId}/value")
        @Operation(summary = "Get current wallet value", description = "Returns the total wallet value and the list of assets with their current values")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Wallet value retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletValueDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Wallet not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<WalletValueDTO> getCurrentWalletValue(
                        @Parameter(description = "Wallet ID", required = true) @PathVariable Long walletId) {

                WalletValueDTO walletValue = walletValuationService.getCurrentWalletValue(walletId);
                return ResponseEntity.ok(walletValue);
        }

        @GetMapping("/{walletId}/value/historical")
        @Operation(summary = "Get historical wallet value", description = "Returns the total wallet value at a specific date and the list of assets with their values on that date")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Historical wallet value retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletValueDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid date in request"),
                        @ApiResponse(responseCode = "404", description = "Wallet not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<WalletValueDTO> getHistoricalWalletValue(
                        @Parameter(description = "Wallet ID", required = true) @PathVariable Long walletId,
                        @Parameter(description = "Date to query historical value", required = true, example = "2024-01-01T10:30:00") @RequestParam LocalDateTime date) {

                WalletValueDTO walletValue = walletValuationService.getHistoricalWalletValue(walletId, date);
                return ResponseEntity.ok(walletValue);
        }

        @GetMapping("/{walletId}/performance")
        @Operation(summary = "Calculate wallet performance", description = "Returns the performance of each asset in the wallet at a specific date")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Wallet performance calculated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WalletPerformanceDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid date in request"),
                        @ApiResponse(responseCode = "404", description = "Wallet not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<WalletPerformanceDTO> calculateWalletPerformance(
                        @Parameter(description = "Wallet ID", required = true) @PathVariable Long walletId,
                        @Parameter(description = "Date to calculate performance", required = true, example = "2024-01-01T10:30:00") @RequestParam LocalDateTime date) {

                WalletPerformanceDTO performance = walletValuationService.calculateWalletPerformance(walletId, date);
                return ResponseEntity.ok(performance);
        }
}
