package com.finassist.ai.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/debug")
public class TikaTestController {

    @PostMapping("/tika-extract")
    public String extract(@RequestParam("file") MultipartFile file) throws Exception {
        TikaDocumentReader reader = new TikaDocumentReader(file.getResource());
        return reader.get().stream()
                .map(Document::getText)
                .reduce("", String::concat);
    }
}