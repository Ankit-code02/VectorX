package com.vectorx.backend.service;

import com.vectorx.backend.algorithm.BruteForce;
import com.vectorx.backend.algorithm.HNSW;
import com.vectorx.backend.algorithm.KDTree;
import com.vectorx.backend.model.VectorItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorDatabase {

    private final int dimensions;

    private final Map<Integer, VectorItem> store =
            new HashMap<>();

    private final BruteForce bruteForce =
            new BruteForce();

    private final KDTree kdTree;

    private final HNSW hnsw;

    private int nextId = 1;

    public VectorDatabase(int dimensions) {

        this.dimensions = dimensions;

        this.kdTree =
                new KDTree(dimensions);

        this.hnsw =
                new HNSW(
                        dimensions,
                        16,
                        200
                );
    }

    public synchronized int insert(
            String metadata,
            String category,
            float[] embedding
    ) {

        if (embedding == null
                || embedding.length != dimensions) {

            throw new IllegalArgumentException(
                    "Expected "
                            + dimensions
                            + " dimensional vector"
            );
        }

        VectorItem vector =
                new VectorItem(
                        nextId++,
                        metadata,
                        category,
                        embedding
                );

        store.put(
                vector.getId(),
                vector
        );

        bruteForce.insert(vector);
        kdTree.insert(vector);
        hnsw.insert(vector);

        return vector.getId();
    }

    public synchronized boolean remove(int id) {

        if (!store.containsKey(id)) {
            return false;
        }

        store.remove(id);

        bruteForce.remove(id);
        hnsw.remove(id);

        /*
         * KDTree does not have a direct delete operation.
         * Rebuild it from the remaining vectors.
         */
        kdTree.rebuild(
                new ArrayList<>(
                        store.values()
                )
        );

        return true;
    }

    public synchronized SearchOutput search(
            float[] query,
            int k,
            String metric,
            String algorithm
    ) {

        if (query == null
                || query.length != dimensions) {

            throw new IllegalArgumentException(
                    "Expected "
                            + dimensions
                            + " dimensional query vector"
            );
        }

        if (k <= 0) {
            throw new IllegalArgumentException(
                    "k must be greater than 0"
            );
        }

        long start =
                System.nanoTime();

        List<RawResult> rawResults;

        String selectedAlgorithm =
                algorithm == null
                        ? "hnsw"
                        : algorithm.toLowerCase();

        String selectedMetric =
                metric == null
                        ? "cosine"
                        : metric.toLowerCase();

        switch (selectedAlgorithm) {

            case "bruteforce" -> {

                List<BruteForce.SearchResult>
                        results =
                        bruteForce.knn(
                                query,
                                k,
                                selectedMetric
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

            case "kdtree" -> {

                List<KDTree.SearchResult>
                        results =
                        kdTree.knn(
                                query,
                                k,
                                selectedMetric
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

            case "hnsw" -> {

                List<HNSW.SearchResult>
                        results =
                        hnsw.knn(
                                query,
                                k,
                                selectedMetric
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

            default -> {

                List<HNSW.SearchResult>
                        results =
                        hnsw.knn(
                                query,
                                k,
                                selectedMetric
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

                selectedAlgorithm = "hnsw";
            }
        }

        long latencyUs =
                (System.nanoTime() - start)
                        / 1_000;

        List<SearchHit> hits =
                new ArrayList<>();

        for (RawResult result : rawResults) {

            VectorItem vector =
                    store.get(result.id());

            if (vector == null) {
                continue;
            }

            hits.add(
                    new SearchHit(
                            vector.getId(),
                            vector.getMetadata(),
                            vector.getCategory(),
                            vector.getEmbedding(),
                            result.distance()
                    )
            );
        }

        return new SearchOutput(
                hits,
                latencyUs,
                selectedAlgorithm,
                selectedMetric
        );
    }

    public synchronized BenchmarkOutput benchmark(
            float[] query,
            int k,
            String metric
    ) {

        if (query == null
                || query.length != dimensions) {

            throw new IllegalArgumentException(
                    "Expected "
                            + dimensions
                            + " dimensional query vector"
            );
        }

        String selectedMetric =
                metric == null
                        ? "cosine"
                        : metric.toLowerCase();

        long bruteForceUs =
                measure(() ->
                        bruteForce.knn(
                                query,
                                k,
                                selectedMetric
                        )
                );

        long kdTreeUs =
                measure(() ->
                        kdTree.knn(
                                query,
                                k,
                                selectedMetric
                        )
                );

        long hnswUs =
                measure(() ->
                        hnsw.knn(
                                query,
                                k,
                                selectedMetric
                        )
                );

        return new BenchmarkOutput(
                bruteForceUs,
                kdTreeUs,
                hnswUs,
                store.size()
        );
    }

    private long measure(
            Runnable operation
    ) {

        long start =
                System.nanoTime();

        operation.run();

        return (
                System.nanoTime() - start
        ) / 1_000;
    }

    public synchronized List<VectorItem> all() {

        return new ArrayList<>(
                store.values()
        );
    }

    public synchronized GraphInfo hnswInfo() {

        return new GraphInfo(
                hnsw.getMaxLevel(),
                hnsw.size(),
                hnsw.getNodesPerLayer(),
                hnsw.getEdgesPerLayer(),
                hnsw.getGraphNodes(),
                hnsw.getGraphEdges()
        );
    }

    public synchronized int size() {
        return store.size();
    }

    public int getDimensions() {
        return dimensions;
    }

    private record RawResult(
            double distance,
            int id
    ) {
    }

    public record SearchHit(
            int id,
            String metadata,
            String category,
            float[] embedding,
            double distance
    ) {
    }

    public record SearchOutput(
            List<SearchHit> hits,
            long latencyUs,
            String algorithm,
            String metric
    ) {
    }

    public record BenchmarkOutput(
            long bruteForceUs,
            long kdTreeUs,
            long hnswUs,
            int itemCount
    ) {
    }

    public record GraphInfo(
            int topLayer,
            int nodeCount,
            Map<Integer, Integer> nodesPerLayer,
            Map<Integer, Integer> edgesPerLayer,
            List<HNSW.GraphNode> nodes,
            List<HNSW.GraphEdge> edges
    ) {
    }
}