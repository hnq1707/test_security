package com.hnq.test_security.service;

import com.hnq.test_security.dto.TransactionDTO;

public interface ITransactionHistoryService {
    void processAndSave(TransactionDTO dto);

}
