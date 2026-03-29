package com.example.wallet.repository;

import com.example.wallet.entity.AssetPriceHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetPriceHistoryEntity, Long> {

    Optional<AssetPriceHistoryEntity> findFirstBySymbolOrderByTimestampDesc(String symbol);

    Optional<AssetPriceHistoryEntity> findFirstBySymbolAndTimestampLessThanEqualOrderByTimestampDesc(
            String symbol, LocalDateTime timestamp);

    Page<AssetPriceHistoryEntity> findBySymbol(String symbol, Pageable pageable);
}
