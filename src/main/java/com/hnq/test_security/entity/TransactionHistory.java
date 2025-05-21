package com.hnq.test_security.entity;

import com.hnq.test_security.utils.AESConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "transaction_history")
public class TransactionHistory {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String transactionId;

    @Convert(converter = AESConverter.class)
    private String account;

    private BigDecimal inDebt;

    private BigDecimal have;

    private LocalDateTime time;

}
