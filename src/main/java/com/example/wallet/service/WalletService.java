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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final RetryService retryService;

    @Transactional
    public WalletResponse createWallet() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.ZERO);
        return WalletResponse.fromWallet(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse processOperation(WalletOperationRequest request) {
        log.debug("Начало обработки операции для кошелька: {}", request.getWalletId());
        try {
            return retryService.executeWithRetry(() -> performOperation(request));
        } catch (ConcurrentOperationException e) {
            log.warn("Не удалось обработать операцию после повторных попыток для кошелька: {}", request.getWalletId());
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке операции для кошелька: {}", request.getWalletId(), e);
            throw new RuntimeException("Не удалось обработать операцию", e);
        }
    }

    @Transactional
    protected WalletResponse performOperation(WalletOperationRequest request) {
        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Кошелек не найден: " + request.getWalletId()));

        try {
            log.debug("Выполнение операции {} для кошелька {} на сумму {}", 
                    request.getOperationType(), request.getWalletId(), request.getAmount());
            
            if (request.getOperationType() == WalletOperationRequest.OperationType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            } else {
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientFundsException("Недостаточно средств для снятия");
                }
                wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
            }
            
            Wallet savedWallet = walletRepository.save(wallet);
            log.debug("Операция успешно выполнена для кошелька {}, новый баланс: {}", 
                    request.getWalletId(), savedWallet.getBalance());
            
            return WalletResponse.fromWallet(savedWallet);
        } catch (OptimisticLockException e) {
            log.debug("Обнаружен конфликт при обновлении кошелька: {}", request.getWalletId());
            throw new ConcurrentOperationException("Обнаружена конкурентная операция, попробуйте еще раз");
        }
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(WalletResponse::fromWallet)
                .orElseThrow(() -> new WalletNotFoundException("Кошелек не найден: " + walletId));
    }
} 