package com.example.wallet.service;

import java.util.UUID;

import com.example.wallet.model.WalletDTO;

public interface WalletService {

    UUID createWallet(String name, Long userId);

    WalletDTO getWallet(UUID walletId);
}
