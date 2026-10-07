package com.vectorx.backend.controller;

import com.vectorx.backend.service.DocumentDatabase;
import com.vectorx.backend.service.OllamaClient;
import com.vectorx.backend.service.VectorDatabase;
import com.vectorx.backend.service.VectorXService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatusController {

    private final OllamaClient ollamaClient;
    private final VectorDatabase vectorDatabase;
    private final DocumentDatabase documentDatabase;

    public StatusController(VectorXService vectorXService) {
        this.ollamaClient = vectorXService.getOllamaClient();
        this.vectorDatabase = vectorXService.getVectorDatabase();
        this.documentDatabase = vectorXService.getDocumentDatabase();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "ollamaAvailable", ollamaClient.isAvailable(),
                "embedModel", ollamaClient.embedModel,
                "genModel", ollamaClient.genModel,
                "docCount", documentDatabase.size(),
                "docDims", documentDatabase.getDimensions(),
                "demoDims", vectorDatabase.getDimensions(),
                "demoCount", vectorDatabase.size()
        );
    }
}