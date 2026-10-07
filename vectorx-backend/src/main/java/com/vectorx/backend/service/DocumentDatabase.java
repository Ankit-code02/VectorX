package com.vectorx.backend.service;

import com.vectorx.backend.algorithm.BruteForce;
import com.vectorx.backend.algorithm.HNSW;
import com.vectorx.backend.model.DocumentItem;
import com.vectorx.backend.model.VectorItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentDatabase {

    private final Map<Integer, DocumentItem> documents =
            new HashMap<>();

    private final BruteForce bruteForce =
            new BruteForce();

    private HNSW hnsw;

    private int nextId = 1;

    private int dimensions = -1;

    public synchronized int insert(
            String title,
            String text,
            float[] embedding
    ) {

        if (embedding == null
                || embedding.length == 0) {

            throw new IllegalArgumentException(
                    "Document embedding cannot be empty"
            );
        }

        if (dimensions == -1) {

            dimensions = embedding.length;

            hnsw = new HNSW(
                    dimensions,
                    16,
                    200
            );

        } else if (embedding.length != dimensions) {

            throw new IllegalArgumentException(
                    "Document embedding dimension mismatch"
            );
        }

        int id = nextId++;

        DocumentItem document =
                new DocumentItem(
                        id,
                        title,
                        text,
                        embedding
                );

        documents.put(
                id,
                document
        );

        /*
         * DocumentDatabase uses the existing vector
         * indexes for document similarity search.
         */
        VectorItem vector =
                new VectorItem(
                        id,
                        title,
                        "document",
                        embedding
                );

        bruteForce.insert(vector);
        hnsw.insert(vector);

        return id;
    }

    public synchronized boolean remove(int id) {

        if (!documents.containsKey(id)) {
            return false;
        }

        documents.remove(id);

        bruteForce.remove(id);

        if (hnsw != null) {
            hnsw.remove(id);
        }

        return true;
    }

    public synchronized List<DocumentSearchResult> search(
            float[] query,
            int k
    ) {

        if (documents.isEmpty()) {
            return new ArrayList<>();
        }

        if (query == null
                || query.length != dimensions) {

            throw new IllegalArgumentException(
                    "Query embedding dimension mismatch"
            );
        }

        if (k <= 0) {
            return new ArrayList<>();
        }

        List<RawResult> rawResults;

        /*
         * The original VectorX document search switches
         * between Brute Force and HNSW based on collection size.
         */
        if (documents.size() < 10) {

            List<BruteForce.SearchResult> results =
                    bruteForce.knn(
                            query,
                            k,
                            "cosine"
                    );

            rawResults =
                    results.stream()
                            .map(result ->
                                    new RawResult(
                                            result.distance(),
                                            result.id()
                                    )
                            )
                            .toList();

        } else {

            List<HNSW.SearchResult> results =
                    hnsw.knn(
                            query,
                            k,
                            "cosine"
                    );

            rawResults =
                    results.stream()
                            .map(result ->
                                    new RawResult(
                                            result.distance(),
                                            result.id()
                                    )
                            )
                            .toList();
        }

        List<DocumentSearchResult> results =
                new ArrayList<>();

        for (RawResult result : rawResults) {

            /*
             * Keep only documents whose cosine distance
             * is within the original document-search threshold.
             */
            if (result.distance() > 0.7) {
                continue;
            }

            DocumentItem document =
                    documents.get(result.id());

            if (document == null) {
                continue;
            }

            results.add(
                    new DocumentSearchResult(
                            document,
                            result.distance()
                    )
            );
        }

        return results;
    }

    public synchronized List<DocumentItem> all() {

        return new ArrayList<>(
                documents.values()
        );
    }

    public synchronized int size() {
        return documents.size();
    }

    public int getDimensions() {
        return dimensions;
    }

    private record RawResult(
            double distance,
            int id
    ) {
    }

    public record DocumentSearchResult(
            DocumentItem document,
            double distance
    ) {
    }
}