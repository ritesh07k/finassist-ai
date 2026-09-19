package com.finassist.ai.controller;

import com.finassist.ai.dto.ApiResponse;
import com.finassist.ai.service.CategoryEnrichmentService;
import com.finassist.ai.service.TransactionEmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrichment")
public class EnrichmentController {

    private final CategoryEnrichmentService enrichmentService;
    private final TransactionEmbeddingService embeddingService;

    public EnrichmentController(CategoryEnrichmentService enrichmentService,
                                 TransactionEmbeddingService embeddingService) {
        this.enrichmentService = enrichmentService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/categorize")
    public ResponseEntity<ApiResponse<String>> categorize() {
        int count = enrichmentService.enrichUncategorized();
        return ResponseEntity.ok(ApiResponse.of("Categorized " + count + " transaction(s)."));
    }

    @PostMapping("/embed")
    public ResponseEntity<ApiResponse<String>> embed() {
        int count = embeddingService.embedAllTransactions();
        return ResponseEntity.ok(ApiResponse.of("Embedded " + count + " transaction(s)."));
    }
}