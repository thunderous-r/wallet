package com.example.wallet.controller;

import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.dto.WalletResponse;
import com.example.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet() {
        return ResponseEntity.ok(walletService.createWallet());
    }

    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processOperation(@Valid @RequestBody WalletOperationRequest request) {
        return ResponseEntity.ok(walletService.processOperation(request));
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }
} 