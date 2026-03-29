package com.example.wallet.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.model.AssetHistoryDTO;
import com.example.wallet.service.AssetHistoryService;
import com.example.wallet.validation.ValidAssetSymbol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/assets/history")
@RequiredArgsConstructor
@Validated
@Tag(name = "Asset History", description = "Operations related to asset price history")
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    @GetMapping("/{symbol}")
    @Operation(summary = "Get price history", description = "Returns the price history of an asset with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetHistoryDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "Asset not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<AssetHistoryDTO>> getAssetHistory(
            @Parameter(description = "Asset symbol (e.g.: BTC, ETH)", required = true) @PathVariable @ValidAssetSymbol String symbol,
            @Parameter(description = "Page number (starting at 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10") @RequestParam(defaultValue = "10") int size) {

        Page<AssetHistoryDTO> history = assetHistoryService.getAssetHistory(symbol, page, size);
        return ResponseEntity.ok(history);
    }

}
