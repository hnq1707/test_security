package com.hnq.test_security.service;

import com.hnq.test_security.dto.TransactionDTO;

/**
 * Giao diện định nghĩa các hành vi xử lý giao dịch giữa các service sử dụng mã hóa RSA.
 *
 * <p>Giao diện này tách biệt logic gửi và nhận giao dịch, đồng thời đảm nhiệm mã hóa và giải mã
 * dữ liệu khi truyền thông tin giữa các microservice.</p>
 */
public interface ITransactionService {

    /**
     * Mã hóa thông tin giao dịch bằng RSA và gửi tới service đích thông qua HTTP.
     *
     * <p>Phương thức này thực hiện:
     * <ul>
     *     <li>Mã hóa từng trường trong {@link TransactionDTO} bằng public key</li>
     *     <li>Gửi DTO đã mã hóa đến một endpoint REST (serviceB/receive)</li>
     *     <li>Ghi log đã được che giấu thông tin nhạy cảm</li>
     * </ul>
     * </p>
     *
     * @param dto Giao dịch đầu vào chưa mã hóa
     * @return Phản hồi từ service đích nếu gửi thành công
     * @throws RuntimeException nếu quá trình mã hóa hoặc gửi thất bại
     */
    String sendEncryptedTransaction(TransactionDTO dto);

    /**
     * Giải mã giao dịch nhận được từ service khác và xử lý lưu trữ.
     *
     * <p>Phương thức này thực hiện:
     * <ul>
     *     <li>Giải mã từng trường trong DTO bằng private key</li>
     *     <li>Gọi đến service xử lý lưu trữ lịch sử giao dịch</li>
     *     <li>Xử lý lỗi và ghi log nếu cần</li>
     * </ul>
     * </p>
     *
     * @param encryptedDto Giao dịch đã được mã hóa từ service khác gửi đến
     * @throws RuntimeException nếu quá trình giải mã hoặc xử lý lưu thất bại
     */
    void decryptAndSaveTransaction(TransactionDTO encryptedDto);
}
