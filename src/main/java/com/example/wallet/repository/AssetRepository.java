package com.example.wallet.repository;

import com.example.wallet.entity.AssetEntity;
import com.example.wallet.model.AssetDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {

        @Query("SELECT DISTINCT a.symbol FROM AssetEntity a")
        List<String> findDistinctSymbols();

        List<AssetEntity> findByWalletId(Long walletId);

        @Query("SELECT DISTINCT a.symbol FROM AssetEntity a")
        List<String> findAllUniqueSymbols();

        @Query("SELECT new com.example.wallet.model.AssetDTO(" +
                        "MIN(a.id), " +
                        "a.symbol, " +
                        "SUM(a.quantity), " +
                        "a.wallet.id) " +
                        "FROM AssetEntity a " +
                        "WHERE a.wallet.id = :walletId " +
                        "GROUP BY a.wallet.id, a.symbol " +
                        "ORDER BY a.symbol")
        List<AssetDTO> findAggregatedAssetsByWalletId(@Param("walletId") Long walletId);

        @Query("SELECT new com.example.wallet.model.AssetDTO(" +
                        "MIN(a.id), " +
                        "a.symbol, " +
                        "a.purchaseDate, " +
                        "SUM(a.quantity), " +
                        "a.wallet.id) " +
                        "FROM AssetEntity a " +
                        "WHERE a.wallet.id = :walletId " +
                        "AND a.purchaseDate <= :date " +
                        "GROUP BY a.wallet.id, a.symbol, a.purchaseDate " +
                        "ORDER BY a.symbol")
        List<AssetDTO> findAggregatedAssetsByWalletIdAndDate(
                        @Param("walletId") Long walletId,
                        @Param("date") LocalDateTime date);
}
