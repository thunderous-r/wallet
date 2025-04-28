package com.example.wallet.service;

import com.example.wallet.exception.ConcurrentOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Slf4j
@Service
public class RetryService {
    @Value("${app.wallet.retry.max-attempts:5}")
    private int maxRetries;

    @Value("${app.wallet.retry.delay-ms:200}")
    private long retryDelayMs;

    @Value("${app.wallet.retry.max-delay-ms:1000}")
    private long maxDelayMs;

    public <T> T executeWithRetry(Callable<T> operation) throws Exception {
        int attempts = 0;
        while (true) {
            try {
                return operation.call();
            } catch (ConcurrentOperationException e) {
                attempts++;
                if (attempts >= maxRetries) {
                    log.warn("Превышено максимальное количество попыток ({}) для операции", maxRetries);
                    throw e;
                }
                long delay = Math.min(retryDelayMs * (1L << (attempts - 1)), maxDelayMs);
                log.debug("Попытка {} из {} не удалась, следующая попытка через {} мс", attempts, maxRetries, delay);
                Thread.sleep(delay);
            }
        }
    }
} 