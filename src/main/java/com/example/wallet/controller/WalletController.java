package com.example.wallet.controller;

import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.dto.WalletResponse;
import com.example.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    // Обработка операций с кошельком (пополнение/снятие)
    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processOperation(@Valid @RequestBody WalletOperationRequest request) {
        WalletResponse response = walletService.processOperation(request);
        return ResponseEntity.ok(response);
    }

    // Получение информации о кошельке
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        WalletResponse response = walletService.getWallet(walletId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet() {
        WalletResponse response = walletService.createWallet();
        return ResponseEntity.ok(response);
    }
} 