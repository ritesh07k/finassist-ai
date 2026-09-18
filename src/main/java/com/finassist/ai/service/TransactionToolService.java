package com.finassist.ai.service;

import com.finassist.ai.model.Transaction;
import com.finassist.ai.model.TransactionType;
import com.finassist.ai.repository.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionToolService {

    private final TransactionRepository transactionRepository;

    public TransactionToolService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(description = "Get the total amount spent (debits only) between two dates, inclusive. Dates must be in yyyy-MM-dd format.")
    public BigDecimal getTotalSpending(String startDate, String endDate) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(
                LocalDate.parse(startDate), LocalDate.parse(endDate));

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();
    }

    @Tool(description = "Get the total amount credited (income/refunds) between two dates, inclusive. Dates must be in yyyy-MM-dd format.")
    public BigDecimal getTotalCredits(String startDate, String endDate) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(
                LocalDate.parse(startDate), LocalDate.parse(endDate));

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = "Get the net change in balance (credits minus debits) between two dates, inclusive. Dates must be in yyyy-MM-dd format.")
    public BigDecimal getNetChange(String startDate, String endDate) {
        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(
                LocalDate.parse(startDate), LocalDate.parse(endDate));

        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Tool(description = "Count how many transactions occurred between two dates, inclusive. Dates must be in yyyy-MM-dd format.")
    public long getTransactionCount(String startDate, String endDate) {
        return transactionRepository.findByTransactionDateBetween(
                LocalDate.parse(startDate), LocalDate.parse(endDate)).size();
    }

    @Tool(description = "Get the total amount spent (debits only) in a specific category between two dates, inclusive. Dates must be in yyyy-MM-dd format. Category must be one of: FOOD_AND_DINING, SHOPPING, BILLS_AND_UTILITIES, TRANSFERS, INCOME, INVESTMENTS, ATM_CASH, ENTERTAINMENT, OTHER.")
public BigDecimal getSpendingByCategory(String startDate, String endDate, String category) {
    List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(
            LocalDate.parse(startDate), LocalDate.parse(endDate));

    return transactions.stream()
            .filter(t -> t.getType() == TransactionType.DEBIT)
            .filter(t -> category.equalsIgnoreCase(t.getCategory()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .abs();
}
}