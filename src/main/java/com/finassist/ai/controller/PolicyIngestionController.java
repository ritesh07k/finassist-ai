package com.finassist.ai.controller;

import com.finassist.ai.service.PolicyIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/policies")
public class PolicyIngestionController {

    private final PolicyIngestionService ingestionService;

    public PolicyIngestionController(
            PolicyIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestPolicy(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Policy file cannot be empty.");
        }

        try {

            // Pass both the PDF and its original filename
            ingestionService.ingestPolicy(
                    file.getResource(),
                    file.getOriginalFilename()
            );

            return ResponseEntity.ok(
                    "Policy successfully ingested: "
                            + file.getOriginalFilename()
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError()
                    .body("Failed to ingest policy: "
                            + e.getMessage());
        }
    }
}