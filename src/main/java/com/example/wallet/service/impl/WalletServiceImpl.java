package com.example.wallet.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.wallet.entity.WalletEntity;
import com.example.wallet.mapper.WalletMapper;
import com.example.wallet.model.WalletDTO;
import com.example.wallet.repository.WalletRepository;
import com.example.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public UUID createWallet(String name, Long userId) {
        WalletEntity wallet = new WalletEntity();
        wallet.setUuid(UUID.randomUUID());
        wallet.setName(name);
        wallet.setUserId(userId);

        WalletEntity savedWallet = walletRepository.save(wallet);
        return savedWallet.getUuid();
    }

    @Override
    public WalletDTO getWallet(Long walletId) {
        WalletEntity wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found with ID: " + walletId));

        return walletMapper.toWalletDTO(wallet);
    }

}
