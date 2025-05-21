package com.hnq.test_security.service;

import com.hnq.test_security.dto.TransactionDTO;
import com.hnq.test_security.entity.TransactionHistory;
import com.hnq.test_security.repository.TransactionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryServiceImpl implements ITransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    @Transactional
    public void processAndSave(TransactionDTO dto) {
        try {
            // Lấy dữ liệu từ DTO
            String transactionId = dto.getTransactionId();
            String fromAccount = dto.getFromAccount();
            String toAccount = dto.getToAccount();
            String inDebtStr = dto.getInDebt();
            String haveStr = dto.getHave();
            String timeStr = dto.getTime();

            // Chuyển đổi kiểu dữ liệu phù hợp
            BigDecimal inDebt = new BigDecimal(inDebtStr);
            BigDecimal have = new BigDecimal(haveStr);
            LocalDateTime time = LocalDateTime.parse(timeStr);

            // 1. Tạo bản ghi giao dịch NỢ cho tài khoản nguồn
            TransactionHistory debitTransaction = new TransactionHistory();
            debitTransaction.setTransactionId(transactionId);
            debitTransaction.setAccount(fromAccount); // AES mã hóa tự xử lý trong
            // converter
            debitTransaction.setInDebt(inDebt);
            debitTransaction.setHave(BigDecimal.ZERO);
            debitTransaction.setTime(time);

            // 2. Tạo bản ghi giao dịch CÓ cho tài khoản đích
            TransactionHistory creditTransaction = new TransactionHistory();
            creditTransaction.setTransactionId(transactionId);
            creditTransaction.setAccount(toAccount); // AES mã hóa tự xử lý trong
            // converter
            creditTransaction.setInDebt(BigDecimal.ZERO);
            creditTransaction.setHave(have);
            creditTransaction.setTime(time);

            // Lưu cả hai bản ghi vào database
            transactionHistoryRepository.save(debitTransaction);
            transactionHistoryRepository.save(creditTransaction);

            log.info("✅ Giao dịch đã lưu thành công: transactionId={}, account={}, inDebt={}, have={}, time={}", 
                mask(transactionId), mask(fromAccount),mask(toAccount),
                    mask(inDebtStr),
                    mask(haveStr),
                    mask(timeStr));
        } catch (DateTimeParseException e) {
            log.error("❌ Lỗi định dạng thời gian không hợp lệ: time=?", e);
            throw new RuntimeException("Thời gian không hợp lệ", e);
        } catch (NumberFormatException e) {
            log.error("❌ Lỗi định dạng số tiền không hợp lệ: inDebt=?, have=?", e);
            throw new RuntimeException("Số tiền không hợp lệ", e);
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý giao dịch: transactionId=?, account=?, inDebt=?, have=?, time=?", e);
            throw new RuntimeException("Xử lý giao dịch thất bại", e);
        }
    }

    /**
     * Che bớt thông tin nhạy cảm khi log.
     */
    private String mask(String value) {
        if (value == null || value.length() < 6) return "****";
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
