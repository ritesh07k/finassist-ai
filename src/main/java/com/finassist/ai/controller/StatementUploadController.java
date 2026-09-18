package com.finassist.ai.controller;

import com.finassist.ai.dto.ExtractedTransaction;
import com.finassist.ai.model.Transaction;
import com.finassist.ai.model.TransactionType;
import com.finassist.ai.repository.TransactionRepository;
import com.finassist.ai.service.TransactionExtractionService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/statements")
public class StatementUploadController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern OPENING_BALANCE_PATTERN =
            Pattern.compile("Opening Balance:\\s*INR\\s*([\\d,]+\\.\\d{2})");

    private final TransactionExtractionService extractionService;
    private final TransactionRepository transactionRepository;

    public StatementUploadController(TransactionExtractionService extractionService,
                                      TransactionRepository transactionRepository) {
        this.extractionService = extractionService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadStatement(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Statement file cannot be empty.");
        }

        try {
            String rawText = extractRawText(file);

            BigDecimal openingBalance = extractOpeningBalance(rawText);
            if (openingBalance == null) {
                return ResponseEntity.badRequest()
                        .body("Could not find an Opening Balance line in the statement — cannot derive transaction directions without it.");
            }

            List<ExtractedTransaction> extracted =
                    extractionService.extractTransactions(rawText);

            List<Transaction> validated = new ArrayList<>();
            List<String> flagged = new ArrayList<>();
            BigDecimal previousBalance = openingBalance;

            for (ExtractedTransaction e : extracted) {
                Transaction t = toEntity(e, previousBalance, file.getOriginalFilename());
                if (t == null) {
                    flagged.add(e.getDescription() + " (unparseable row)");
                } else {
                    validated.add(t);
                }
                // advance the running balance using the LLM's reported balance,
                // regardless of whether this row parsed cleanly, so a single bad
                // row doesn't cascade into every row after it
                if (e.getBalance() != null) {
                    previousBalance = e.getBalance();
                }
            }

            transactionRepository.saveAll(validated);

            String message = "Ingested " + validated.size() + " of " + extracted.size()
                    + " extracted rows from: " + file.getOriginalFilename();

            if (!flagged.isEmpty()) {
                message += ". Flagged " + flagged.size() + " row(s): " + flagged;
            }

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to process statement: " + e.getMessage());
        }
    }

    private String extractRawText(MultipartFile file) throws Exception {
        TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
        return reader.get().stream()
                .map(Document::getText)
                .reduce("", String::concat);
    }

    private BigDecimal extractOpeningBalance(String rawText) {
        Matcher matcher = OPENING_BALANCE_PATTERN.matcher(rawText);
        if (matcher.find()) {
            String value = matcher.group(1).replace(",", "");
            return new BigDecimal(value);
        }
        return null;
    }

    private Transaction toEntity(ExtractedTransaction e, BigDecimal previousBalance, String sourceStatement) {
        try {
            LocalDate date = LocalDate.parse(e.getDate().trim(), DATE_FORMAT);
            BigDecimal balance = e.getBalance();

            if (balance == null || previousBalance == null) {
                return null;
            }

            BigDecimal amount = balance.subtract(previousBalance);
            TransactionType type = amount.signum() < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;

            return new Transaction(date, e.getDescription().trim(), amount, type, balance, sourceStatement);

        } catch (Exception ex) {
            return null;
        }
    }
}