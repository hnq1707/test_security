package com.hnq.test_security.dto;

import lombok.Data;

@Data
public class TransactionDTO {
    private String fromAccount;
    private String transactionId;
    private String toAccount;
    private String inDebt;
    private String have;
    private String time;
}
