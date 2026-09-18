package com.finassist.ai.service;

import com.finassist.ai.model.Transaction;
import com.finassist.ai.model.TransactionType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatementParsingService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<Transaction> parseCsv(InputStream inputStream, String sourceStatement) throws IOException {

        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // skip header row
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length < 5) {
                    continue; // skip malformed rows rather than fail the whole batch
                }

                LocalDate date = LocalDate.parse(fields[0].trim(), DATE_FORMAT);
                String description = fields[1].trim();
                String debitStr = fields[2].trim();
                String creditStr = fields[3].trim();
                BigDecimal balanceAfter = new BigDecimal(fields[4].trim());

                BigDecimal amount;
                TransactionType type;

                if (!debitStr.isEmpty()) {
                    amount = new BigDecimal(debitStr).negate();
                    type = TransactionType.DEBIT;
                } else if (!creditStr.isEmpty()) {
                    amount = new BigDecimal(creditStr);
                    type = TransactionType.CREDIT;
                } else {
                    continue; // row with neither debit nor credit — skip
                }

                transactions.add(new Transaction(
                        date, description, amount, type, balanceAfter, sourceStatement
                ));
            }
        }

        return transactions;
    }
}