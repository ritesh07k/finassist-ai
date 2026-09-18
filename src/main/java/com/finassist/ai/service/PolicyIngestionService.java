package com.finassist.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PolicyIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public PolicyIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = TokenTextSplitter.builder().build();
    }

    public void ingestPolicy(Resource resource, String filename) {

        // Read the PDF and convert it into Spring AI Documents
        TikaDocumentReader reader = new TikaDocumentReader(resource);

        List<Document> documents = reader.get();

        // Derive category from filename
        String category = filename
                .replace(".pdf", "")
                .replace("-policy", "")
                .replace("_policy", "");

        // Add metadata to every document
        documents = documents.stream()
                .map(document -> new Document(
                        document.getText(),
                        Map.of(
                                "source", filename,
                                "category", category,
                                "type", "policy"
                        )
                ))
                .toList();

        // Split documents into smaller chunks
        List<Document> chunks = textSplitter.apply(documents);

        // Store chunks and generate embeddings automatically
        vectorStore.add(chunks);
    }
}