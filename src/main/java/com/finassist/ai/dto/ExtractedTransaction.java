package com.finassist.ai.dto;

import java.math.BigDecimal;

public class ExtractedTransaction {

    private String date;
    private String description;
    private BigDecimal balance;

    public ExtractedTransaction() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}