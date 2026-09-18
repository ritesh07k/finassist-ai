package com.finassist.ai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate transactionDate;

    private String description;

    // Signed: negative = debit, positive = credit
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    // Nullable until the categorization/enrichment step runs
    private String category;

    private BigDecimal balanceAfter;

    private String sourceStatement;

    protected Transaction() {
        // required by JPA
    }

    public Transaction(LocalDate transactionDate, String description, BigDecimal amount,
                        TransactionType type, BigDecimal balanceAfter, String sourceStatement) {
        this.transactionDate = transactionDate;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.sourceStatement = sourceStatement;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getSourceStatement() {
        return sourceStatement;
    }

    public void setSourceStatement(String sourceStatement) {
        this.sourceStatement = sourceStatement;
    }
}