package com.example.wallet.service;

import com.example.wallet.domain.Wallet;
import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.dto.WalletResponse;
import com.example.wallet.exception.ConcurrentOperationException;
import com.example.wallet.exception.InsufficientFundsException;
import com.example.wallet.exception.WalletNotFoundException;
import com.example.wallet.repository.WalletRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    @Transactional
    public WalletResponse createWallet() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.ZERO);
        return WalletResponse.fromWallet(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse processOperation(WalletOperationRequest request) {
        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + request.getWalletId()));

        try {
            if (request.getOperationType() == WalletOperationRequest.OperationType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            } else {
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientFundsException("Insufficient funds for withdrawal");
                }
                wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            }
            return WalletResponse.fromWallet(walletRepository.save(wallet));
        } catch (OptimisticLockException e) {
            throw new ConcurrentOperationException("Concurrent operation detected, please retry");
        }
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(WalletResponse::fromWallet)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }
} 