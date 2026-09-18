package com.finassist.ai.repository;

import com.finassist.ai.model.Transaction;
import com.finassist.ai.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findBySourceStatement(String sourceStatement);

    List<Transaction> findByTransactionDateBetween(LocalDate start, LocalDate end);

    List<Transaction> findByType(TransactionType type);

    List<Transaction> findByCategory(String category);

    List<Transaction> findByCategoryIsNull();
}