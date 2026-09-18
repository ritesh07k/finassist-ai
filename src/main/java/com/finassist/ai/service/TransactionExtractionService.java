package com.finassist.ai.service;

import com.finassist.ai.dto.ExtractedTransaction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionExtractionService {

    private static final int CHUNK_LINE_SIZE = 10;

    private final ChatClient chatClient;

    public TransactionExtractionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<ExtractedTransaction> extractTransactions(String rawStatementText) {

        List<String> lines = List.of(rawStatementText.split("\\R"));
        List<String> transactionLines = filterLikelyTransactionLines(lines);

        List<ExtractedTransaction> allResults = new ArrayList<>();

        for (int i = 0; i < transactionLines.size(); i += CHUNK_LINE_SIZE) {
            int end = Math.min(i + CHUNK_LINE_SIZE, transactionLines.size());
            List<String> chunk = transactionLines.subList(i, end);
            String chunkText = String.join("\n", chunk);

            List<ExtractedTransaction> chunkResult = extractChunk(chunkText);
            allResults.addAll(chunkResult);
        }

        return allResults;
    }

    private List<ExtractedTransaction> extractChunk(String chunkText) {

        BeanOutputConverter<List<ExtractedTransaction>> converter =
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<ExtractedTransaction>>() {});

        String format = converter.getFormat();

        String promptText = """
            Extract each bank transaction row from the text below into structured data.
            For each line, extract only the date, the description, and the running
            balance (the last number on the line). Ignore the debit and credit
            columns entirely — do not extract them.

            Preserve numeric values exactly as shown, without currency symbols or commas.

            Text:
            %s

            %s
            """.formatted(chunkText, format);

        String response = chatClient
                .prompt()
                .user(promptText)
                .call()
                .content();

        return converter.convert(response);
    }

    private List<String> filterLikelyTransactionLines(List<String> lines) {
        return lines.stream()
                .filter(line -> line.matches("^\\d{2}/\\d{2}/\\d{4}.*"))
                .toList();
    }
}