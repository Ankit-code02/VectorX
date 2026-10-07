package com.vectorx.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class VectorXService {

    private static final int DIMS = 16;

    private final VectorDatabase vectorDatabase;
    private final DocumentDatabase documentDatabase;
    private final OllamaClient ollamaClient;

    public VectorXService() {
        this.vectorDatabase = new VectorDatabase(DIMS);
        this.documentDatabase = new DocumentDatabase();
        this.ollamaClient = new OllamaClient();
    }

    @PostConstruct
    public void initialize() {
        DemoData.loadDemo(vectorDatabase);
    }

    public VectorDatabase getVectorDatabase() {
        return vectorDatabase;
    }

    public DocumentDatabase getDocumentDatabase() {
        return documentDatabase;
    }

    public OllamaClient getOllamaClient() {
        return ollamaClient;
    }
}