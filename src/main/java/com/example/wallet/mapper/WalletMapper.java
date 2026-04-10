package com.example.wallet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.wallet.entity.WalletEntity;
import com.example.wallet.model.WalletDTO;

@Mapper(componentModel = "spring", uses = { AssetMapper.class })
public interface WalletMapper {

    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "assets", target = "assets")
    WalletDTO toWalletDTO(WalletEntity wallet);

}
