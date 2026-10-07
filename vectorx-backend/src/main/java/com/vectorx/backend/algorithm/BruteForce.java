package com.vectorx.backend.algorithm;

import com.vectorx.backend.model.VectorItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BruteForce {

    private final List<VectorItem> items = new ArrayList<>();

    public void insert(VectorItem vector) {
        items.add(vector);
    }

    public List<SearchResult> knn(float[] query, int k, String metric) {

        List<SearchResult> results = new ArrayList<>();

        for (VectorItem item : items) {

            double distance = calculateDistance(
                    query,
                    item.getEmbedding(),
                    metric
            );

            results.add(
                    new SearchResult(
                            distance,
                            item.getId()
                    )
            );
        }

        results.sort(
                Comparator.comparingDouble(SearchResult::distance)
        );

        if (results.size() > k) {
            return new ArrayList<>(results.subList(0, k));
        }

        return results;
    }

    public void remove(int id) {
        items.removeIf(item -> item.getId() == id);
    }

    private double calculateDistance(
            float[] query,
            float[] embedding,
            String metric
    ) {

        if (metric == null) {
            metric = "cosine";
        }

        return switch (metric.toLowerCase()) {

            case "cosine" ->
                    DistanceMetrics.cosine(query, embedding);

            case "euclidean" ->
                    DistanceMetrics.euclidean(query, embedding);

            case "manhattan" ->
                    DistanceMetrics.manhattan(query, embedding);

            default ->
                    DistanceMetrics.cosine(query, embedding);
        };
    }

    public List<VectorItem> getItems() {
        return new ArrayList<>(items);
    }

    public record SearchResult(
            double distance,
            int id
    ) {
    }
}