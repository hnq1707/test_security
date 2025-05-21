package com.hnq.test_security.service.impl;

import com.hnq.test_security.dto.TransactionDTO;
import com.hnq.test_security.entity.TransactionHistory;
import com.hnq.test_security.repository.TransactionHistoryRepository;
import com.hnq.test_security.service.ITransactionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Triển khai của {@link ITransactionHistoryService} để xử lý và lưu lịch sử giao dịch.
 *
 * <p>Giao dịch được phân tách thành 2 bản ghi:
 * <ul>
 *   <li>Tài khoản gửi: inDebt = amount, have = 0</li>
 *   <li>Tài khoản nhận: inDebt = 0, have = amount</li>
 * </ul>
 * Dữ liệu đầu vào được kiểm tra định dạng và chuẩn hóa trước khi lưu.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryServiceImpl implements ITransactionHistoryService {

    private final String ZERO_AMOUNT = "0";
    private final String SPECIAL_STRING = "****";
    private final TransactionHistoryRepository transactionHistoryRepository;

    /**
     * Xử lý dữ liệu giao dịch và lưu vào cơ sở dữ liệu dưới dạng hai bản ghi:
     * một cho tài khoản gửi (ghi nợ) và một cho tài khoản nhận (ghi có).
     *
     * @param dto DTO chứa thông tin giao dịch đã được giải mã RSA
     * @throws RuntimeException nếu dữ liệu không hợp lệ hoặc lỗi trong quá trình lưu
     */
    @Override
    @Transactional
    public void processAndSave(TransactionDTO dto) {
        try {
            String transactionId = dto.getTransactionId();
            String fromAccount = dto.getFromAccount();
            String toAccount = dto.getToAccount();
            BigDecimal amount = parseAmount(dto.getInDebt(), dto.getHave());
            LocalDateTime time = parseTime(dto.getTime());

            TransactionHistory fromTransaction = createTransaction(transactionId, fromAccount, amount, BigDecimal.ZERO, time);
            TransactionHistory toTransaction = createTransaction(transactionId, toAccount, BigDecimal.ZERO, amount, time);

            transactionHistoryRepository.save(fromTransaction);
            transactionHistoryRepository.save(toTransaction);

            log.info("✅ Đã lưu giao dịch: id={}, from={}, to={}, amount={}, time={}",
                    mask(transactionId), mask(fromAccount), mask(toAccount), mask(amount.toPlainString()), time);

        } catch (DateTimeParseException e) {
            log.error("❌ Lỗi định dạng thời gian: {}", dto.getTime(), e);
            throw new RuntimeException("Thời gian không hợp lệ", e);
        } catch (NumberFormatException e) {
            log.error("❌ Lỗi định dạng số: inDebt={}, have={}", dto.getInDebt(), dto.getHave(), e);
            throw new RuntimeException("Số tiền không hợp lệ", e);
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý giao dịch", e);
            throw new RuntimeException("Xử lý giao dịch thất bại", e);
        }
    }

    /**
     * Tạo một bản ghi giao dịch mới.
     *
     * @param transactionId Mã giao dịch
     * @param account Tài khoản liên quan
     * @param inDebt Số tiền ghi nợ
     * @param have Số tiền ghi có
     * @param time Thời gian giao dịch
     * @return Đối tượng {@link TransactionHistory} đã tạo
     */
    private TransactionHistory createTransaction(String transactionId, String account, BigDecimal inDebt, BigDecimal have, LocalDateTime time) {
        TransactionHistory tx = new TransactionHistory();
        tx.setTransactionId(transactionId);
        tx.setAccount(account);
        tx.setInDebt(inDebt);
        tx.setHave(have);
        tx.setTime(time);
        return tx;
    }

    /**
     * Parse số tiền từ chuỗi, ưu tiên inDebt trước have.
     *
     * @param inDebtStr Giá trị inDebt dạng chuỗi
     * @param haveStr Giá trị have dạng chuỗi
     * @return Giá trị hợp lệ dưới dạng {@link BigDecimal}
     * @throws IllegalArgumentException nếu cả hai đều rỗng hoặc bằng 0
     */
    private BigDecimal parseAmount(String inDebtStr, String haveStr) {
        if (inDebtStr != null && !inDebtStr.equals(ZERO_AMOUNT)) {
            return new BigDecimal(inDebtStr);
        }
        if (haveStr != null && !haveStr.equals(ZERO_AMOUNT)) {
            return new BigDecimal(haveStr);
        }
        throw new IllegalArgumentException("Phải có inDebt hoặc have > 0");
    }

    /**
     * Parse chuỗi thời gian theo định dạng mặc định ISO-8601.
     *
     * @param timeStr Chuỗi thời gian
     * @return {@link LocalDateTime} đã phân tích
     * @throws DateTimeParseException nếu định dạng không hợp lệ
     */
    private LocalDateTime parseTime(String timeStr) {
        return LocalDateTime.parse(timeStr);
    }

    /**
     * Ẩn bớt thông tin nhạy cảm (mã giao dịch, số tài khoản, v.v.) trong log.
     *
     * @param value Giá trị cần che
     * @return Giá trị đã được che định dạng: "ab****yz"
     */
    private String mask(String value) {
        if (value == null || value.length() < 6) return SPECIAL_STRING;
        return value.substring(0, 2) + SPECIAL_STRING + value.substring(value.length() - 2);
    }
}
