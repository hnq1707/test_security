package com.hnq.test_security.repository;

import com.hnq.test_security.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionHistoryRepository extends
                                              JpaRepository<TransactionHistory, UUID> {
}
