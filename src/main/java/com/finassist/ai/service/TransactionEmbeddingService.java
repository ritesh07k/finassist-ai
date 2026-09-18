package com.finassist.ai.service;

import com.finassist.ai.model.Transaction;
import com.finassist.ai.repository.TransactionRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransactionEmbeddingService {

    private final VectorStore vectorStore;
    private final TransactionRepository transactionRepository;

    public TransactionEmbeddingService(VectorStore vectorStore,
                                        TransactionRepository transactionRepository) {
        this.vectorStore = vectorStore;
        this.transactionRepository = transactionRepository;
    }

    public int embedAllTransactions() {

    // Remove any previously embedded transactions first, so re-running
    // this method is idempotent rather than creating duplicates.
    vectorStore.delete("type == 'transaction'");

    List<Transaction> transactions = transactionRepository.findAll();

    List<Document> documents = transactions.stream()
            .map(t -> new Document(
                    t.getDescription(),
                    Map.of(
                            "type", "transaction",
                            "transaction_id", t.getId().toString(),
                            "category", t.getCategory() != null ? t.getCategory() : "UNCATEGORIZED",
                            "amount", t.getAmount().toString(),
                            "date", t.getTransactionDate().toString()
                    )
            ))
            .toList();

    vectorStore.add(documents);

    return documents.size();
}

    
}