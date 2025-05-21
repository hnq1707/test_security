package com.hnq.test_security.service.impl;

import com.hnq.test_security.dto.TransactionDTO;
import com.hnq.test_security.service.ITransactionHistoryService;
import com.hnq.test_security.service.ITransactionService;
import com.hnq.test_security.utils.RSAEncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Triển khai dịch vụ xử lý giao dịch sử dụng mã hóa RSA.
 *
 * <p>Dịch vụ này bao gồm:
 * <ul>
 *   <li>Mã hóa giao dịch và gửi tới service đích</li>
 *   <li>Giải mã giao dịch nhận được và lưu trữ vào hệ thống</li>
 * </ul>
 *
 * <p>Sử dụng khóa công khai và bí mật từ {@link RSAEncryptionUtils}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ITransactionHistoryService transactionHistoryService;

    /**
     * Mã hóa dữ liệu giao dịch bằng RSA và gửi đến serviceB.
     *
     * @param dto Dữ liệu giao dịch (chưa mã hóa)
     * @return Phản hồi từ serviceB sau khi nhận giao dịch
     * @throws RuntimeException nếu quá trình mã hóa hoặc gửi gặp lỗi
     */
    @Override
    public String sendEncryptedTransaction(TransactionDTO dto) {
        try {
            TransactionDTO encryptedDto = new TransactionDTO();
            encryptedDto.setTransactionId(RSAEncryptionUtils.encrypt(dto.getTransactionId(), publicKey));
            encryptedDto.setFromAccount(RSAEncryptionUtils.encrypt(dto.getFromAccount(), publicKey));
            encryptedDto.setToAccount(RSAEncryptionUtils.encrypt(dto.getToAccount(), publicKey));
            encryptedDto.setInDebt(RSAEncryptionUtils.encrypt(dto.getInDebt(), publicKey));
            encryptedDto.setHave(RSAEncryptionUtils.encrypt(dto.getHave(), publicKey));
            encryptedDto.setTime(RSAEncryptionUtils.encrypt(dto.getTime(), publicKey));
            log.info("✅ Gửi giao dịch thành công. transactionId={}, from={}, to={}",
                    mask(dto.getTransactionId()), mask(dto.getFromAccount()), mask(dto.getToAccount()));
            String url = "http://localhost:8080/api/serviceB/receive";
            ResponseEntity<String> response = restTemplate.postForEntity(url, encryptedDto, String.class);



            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Lỗi gửi giao dịch: {}", e.getMessage(), e);
            throw new RuntimeException("Gửi giao dịch thất bại", e);
        }
    }

    /**
     * Giải mã dữ liệu giao dịch nhận được và gửi đến dịch vụ lưu lịch sử.
     *
     * @param encryptedDto Giao dịch đã được mã hóa bằng RSA
     * @throws RuntimeException nếu giải mã thất bại hoặc lưu không thành công
     */
    @Override
    public void decryptAndSaveTransaction(TransactionDTO encryptedDto) {
        try {
            log.info("✅ Request: transactionId={}, from={}, to={}, " +
                            "response={}",
                    encryptedDto.getTransactionId(),
                    encryptedDto.getFromAccount(),
                    encryptedDto.getToAccount(), encryptedDto.getTime() );
            TransactionDTO decryptedDto = new TransactionDTO();
            decryptedDto.setTransactionId(RSAEncryptionUtils.decrypt(encryptedDto.getTransactionId(), privateKey));
            decryptedDto.setFromAccount(RSAEncryptionUtils.decrypt(encryptedDto.getFromAccount(), privateKey));
            decryptedDto.setToAccount(RSAEncryptionUtils.decrypt(encryptedDto.getToAccount(), privateKey));
            decryptedDto.setInDebt(RSAEncryptionUtils.decrypt(encryptedDto.getInDebt(), privateKey));
            decryptedDto.setHave(RSAEncryptionUtils.decrypt(encryptedDto.getHave(), privateKey));
            decryptedDto.setTime(RSAEncryptionUtils.decrypt(encryptedDto.getTime(), privateKey));

            transactionHistoryService.processAndSave(decryptedDto);
        } catch (Exception e) {
            log.error("❌ Lỗi giải mã hoặc xử lý giao dịch: {}", e.getMessage(), e);
            throw new RuntimeException("Xử lý giao dịch thất bại", e);
        }
    }

    /**
     * Che bớt thông tin nhạy cảm trong log (ví dụ: transactionId, account).
     *
     * @param value Chuỗi cần che
     * @return Chuỗi đã được làm mờ (mask) dạng: "12****89"
     */
    private String mask(String value) {
        if (value == null || value.length() < 6) return "****";
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
