package com.example.wallet.mapper;

import org.mapstruct.Mapper;

import com.example.wallet.entity.AssetPriceHistoryEntity;
import com.example.wallet.model.AssetHistoryDTO;

@Mapper(componentModel = "spring")
public interface AssetHistoryMapper {

    AssetHistoryDTO toAssetHistoryDTO(AssetPriceHistoryEntity assetHistory);

}
