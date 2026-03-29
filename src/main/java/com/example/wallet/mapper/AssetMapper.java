package com.example.wallet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.wallet.entity.AssetEntity;
import com.example.wallet.model.AssetDTO;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    @Mapping(source = "wallet.id", target = "walletId")
    AssetDTO toAssetDTO(AssetEntity asset);

}
