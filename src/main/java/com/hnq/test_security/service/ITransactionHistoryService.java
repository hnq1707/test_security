package com.hnq.test_security.service;

import com.hnq.test_security.dto.TransactionDTO;

/**
 * Giao diện định nghĩa các hành vi xử lý và lưu trữ lịch sử giao dịch.
 *
 * <p>Triển khai interface này để thực hiện:
 * <ul>
 *   <li>Chuyển đổi dữ liệu giao dịch từ DTO</li>
 *   <li>Ghi nhận lịch sử giao dịch vào cơ sở dữ liệu</li>
 * </ul>
 */
public interface ITransactionHistoryService {

    /**
     * Xử lý và lưu trữ lịch sử giao dịch.
     *
     * <p>Phương thức này thực hiện:
     * <ul>
     *   <li>Chuyển đổi dữ liệu từ {@link TransactionDTO} sang entity</li>
     *   <li>Tách giao dịch thành 2 bản ghi: Nợ (tài khoản nguồn) và Có (tài khoản đích)</li>
     *   <li>Lưu cả hai bản ghi vào cơ sở dữ liệu</li>
     * </ul>
     *
     * @param dto Giao dịch đã giải mã, chứa thông tin cần lưu trữ
     * @throws RuntimeException nếu có lỗi trong quá trình xử lý hoặc lưu
     */
    void processAndSave(TransactionDTO dto);
}
