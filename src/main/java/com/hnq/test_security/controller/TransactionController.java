package com.hnq.test_security.controller;

import com.hnq.test_security.dto.TransactionDTO;
import com.hnq.test_security.service.ITransactionHistoryService;
import com.hnq.test_security.service.ITransactionService;
import com.hnq.test_security.utils.RSAEncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final ITransactionService transactionService;

    @PostMapping("/serviceA/send")
    public ResponseEntity<String> sendTransaction(@RequestBody TransactionDTO dto) {
        String response = transactionService.sendEncryptedTransaction(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/serviceB/receive")
    public ResponseEntity<String> receiveTransaction(@RequestBody TransactionDTO dto) {
        transactionService.decryptAndSaveTransaction(dto);
        return ResponseEntity.ok("Đã nhận và lưu thành công");
    }
}

