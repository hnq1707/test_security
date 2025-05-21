package com.hnq.test_security.controller;

import com.hnq.test_security.dto.TransactionDTO;
import com.hnq.test_security.service.ITransactionHistoryService;
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

    private final ITransactionHistoryService transactionService;
    private final RestTemplate restTemplate = new RestTemplate();

    private final PublicKey publicKey;    // Inject từ RSAConfig
    private final PrivateKey privateKey;  // Inject từ RSAConfig

    @GetMapping("/serviceA/send")
    public ResponseEntity<String> sendTransaction() throws Exception {
        ResponseEntity<String> response = null;
        try {

            String transactionId = "TX123456";
            String fromAccount = "9876543210";
            String toAccount = "1234567890";
            String inDebt = "1000000";
            String have = "0";
            String time = LocalDateTime.now().toString();

            TransactionDTO dto = new TransactionDTO();

            // Mã hóa từng trường bằng publicKey
            dto.setTransactionId(RSAEncryptionUtils.encrypt(transactionId, publicKey));
            dto.setFromAccount(RSAEncryptionUtils.encrypt(fromAccount,
                    publicKey));
            dto.setToAccount(RSAEncryptionUtils.encrypt(toAccount, publicKey));
            dto.setInDebt(RSAEncryptionUtils.encrypt(inDebt, publicKey));
            dto.setHave(RSAEncryptionUtils.encrypt(have, publicKey));
            dto.setTime(RSAEncryptionUtils.encrypt(time, publicKey));

            String url = "http://localhost:8080/api/serviceB/receive";

            response = restTemplate.postForEntity(url, dto, String.class);

            log.info("✅ Đã gửi giao dịch mã hóa RSA tới serviceB: transactionId=?, account=?, inDebt=?, have=?, time=?, response={}", 
                response.getBody());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi giao dịch: transactionId=?, account=?, inDebt=?, have=?, time=?", e);
            throw new RuntimeException("Gửi giao dịch thất bại", e);
        }

        return ResponseEntity.ok(response != null ? response.getBody() : "Gửi giao dịch thất bại");
    }

    @PostMapping("/serviceB/receive")
    public ResponseEntity<String> receiveTransaction(@RequestBody TransactionDTO encryptedDto) throws Exception {
        try {
            // Giải mã các trường dữ liệu bằng privateKey trước khi xử lý
            String transactionId = RSAEncryptionUtils.decrypt(encryptedDto.getTransactionId(), privateKey);
            String toAccount =
                    RSAEncryptionUtils.decrypt(encryptedDto.getToAccount(),
                    privateKey);
            String fromAccount = RSAEncryptionUtils.decrypt(encryptedDto.getFromAccount(), privateKey);
            String inDebt = RSAEncryptionUtils.decrypt(encryptedDto.getInDebt(), privateKey);
            String have = RSAEncryptionUtils.decrypt(encryptedDto.getHave(), privateKey);
            String time = RSAEncryptionUtils.decrypt(encryptedDto.getTime(), privateKey);

            // Tạo DTO mới chứa dữ liệu đã giải mã để service xử lý lưu trữ hoặc xử lý tiếp
            TransactionDTO decryptedDto = new TransactionDTO();
            decryptedDto.setTransactionId(transactionId);
            decryptedDto.setFromAccount(fromAccount);
            decryptedDto.setToAccount(toAccount);
            decryptedDto.setInDebt(inDebt);
            decryptedDto.setHave(have);
            decryptedDto.setTime(time);

            transactionService.processAndSave(decryptedDto);
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý giao dịch: transactionId=?, account=?, inDebt=?, have=?, time=?", e);
            throw new RuntimeException("Xử lý giao dịch thất bại", e);
        }

        return ResponseEntity.ok("Đã nhận và lưu thành công");
    }
}
