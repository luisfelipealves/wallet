package com.example.wallet.service;

import com.example.wallet.model.WalletDTO;

public interface WalletService {

    Long createWallet(String name, Long userId);

    WalletDTO getWallet(Long walletId);
}
