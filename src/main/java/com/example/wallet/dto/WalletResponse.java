package com.example.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WalletResponse {
    private UUID id;
    private BigDecimal balance;

    public static WalletResponse fromWallet(com.example.wallet.domain.Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setBalance(wallet.getBalance());
        return response;
    }
} 