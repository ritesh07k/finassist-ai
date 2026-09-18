package com.finassist.ai.service;

import com.finassist.ai.model.Transaction;
import com.finassist.ai.model.TransactionCategory;
import com.finassist.ai.repository.TransactionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CategoryEnrichmentService {

    private static final int MAX_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;

    public CategoryEnrichmentService(ChatClient.Builder chatClientBuilder,
                                      TransactionRepository transactionRepository) {
        this.chatClient = chatClientBuilder.build();
        this.transactionRepository = transactionRepository;
    }

    public int enrichUncategorized() {

        List<Transaction> uncategorized = transactionRepository.findByCategoryIsNull();
        int enrichedCount = 0;

        for (Transaction t : uncategorized) {
            TransactionCategory category = classify(t.getDescription(), t.getType().name());
            t.setCategory(category.name());
            transactionRepository.save(t);
            enrichedCount++;
        }

        return enrichedCount;
    }

    private TransactionCategory classify(String description, String type) {

        String categoryList = Arrays.stream(TransactionCategory.values())
                .map(Enum::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String basePrompt = """
                Classify this bank transaction into exactly one category from
                this list: %s

                Transaction description: "%s"
                Transaction type: %s

                Respond with only the category name, exactly as written above,
                and nothing else.
                """.formatted(categoryList, description, type);

        String prompt = basePrompt;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim();

            try {
                return TransactionCategory.valueOf(response);
            } catch (IllegalArgumentException ex) {
                prompt = basePrompt + "\n\nYour previous answer was: \"" + response
                        + "\". This is not a valid category from the list. "
                        + "Respond with only the exact category name, nothing else.";
            }
        }

        return TransactionCategory.OTHER;
    }
}
