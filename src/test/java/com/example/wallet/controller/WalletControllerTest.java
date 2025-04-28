package com.example.wallet.controller;

import com.example.wallet.dto.WalletOperationRequest;
import com.example.wallet.dto.WalletResponse;
import com.example.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testDepositOperation() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.DEPOSIT);
        request.setAmount(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId.toString()))
                .andExpect(jsonPath("$.balance").value("1000.00"));
    }

    @Test
    public void testWithdrawOperation() throws Exception {
        UUID walletId = UUID.randomUUID();
        WalletOperationRequest request = new WalletOperationRequest();
        request.setWalletId(walletId);
        request.setOperationType(WalletOperationRequest.OperationType.WITHDRAW);
        request.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недостаточно средств для снятия"));
    }

    @Test
    public void testGetWallet() throws Exception {
        UUID walletId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Кошелек не найден: " + walletId));
    }

    @Test
    public void testInvalidRequest() throws Exception {
        // Тест с отсутствующими обязательными полями
        WalletOperationRequest request = new WalletOperationRequest();

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testInvalidUUIDFormat() throws Exception {
        // Тест с некорректным форматом UUID
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"walletId\":\"1\",\"operationType\":\"DEPOSIT\",\"amount\":1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректный формат UUID"));
    }
} 