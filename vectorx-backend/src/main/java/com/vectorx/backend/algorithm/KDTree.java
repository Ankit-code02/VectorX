package com.vectorx.backend.algorithm;

import com.vectorx.backend.model.VectorItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KDTree {

    private final int dimensions;
    private Node root;

    public KDTree(int dimensions) {
        this.dimensions = dimensions;
    }

    public void insert(VectorItem vector) {
        root = insertRecursive(root, vector, 0);
    }

    private Node insertRecursive(Node node, VectorItem vector, int depth) {

        if (node == null) {
            return new Node(vector, depth % dimensions);
        }

        int axis = node.axis;

        if (vector.getEmbedding()[axis] < node.vector.getEmbedding()[axis]) {
            node.left = insertRecursive(
                    node.left,
                    vector,
                    depth + 1
            );
        } else {
            node.right = insertRecursive(
                    node.right,
                    vector,
                    depth + 1
            );
        }

        return node;
    }

    public List<SearchResult> knn(
            float[] query,
            int k,
            String metric
    ) {

        if (root == null || k <= 0) {
            return new ArrayList<>();
        }

        PriorityQueue<SearchResult> best = new PriorityQueue<>(
                Comparator.comparingDouble(SearchResult::distance).reversed()
        );

        searchRecursive(
                root,
                query,
                k,
                metric,
                best
        );

        List<SearchResult> results = new ArrayList<>(best);

        results.sort(
                Comparator.comparingDouble(SearchResult::distance)
        );

        return results;
    }

    private void searchRecursive(
            Node node,
            float[] query,
            int k,
            String metric,
            PriorityQueue<SearchResult> best
    ) {

        if (node == null) {
            return;
        }

        double distance = calculateDistance(
                query,
                node.vector.getEmbedding(),
                metric
        );

        if (best.size() < k) {

            best.offer(
                    new SearchResult(
                            distance,
                            node.vector.getId()
                    )
            );

        } else if (distance < best.peek().distance()) {

            best.poll();

            best.offer(
                    new SearchResult(
                            distance,
                            node.vector.getId()
                    )
            );
        }

        int axis = node.axis;

        double difference =
                query[axis] - node.vector.getEmbedding()[axis];

        Node near;
        Node far;

        if (difference < 0) {
            near = node.left;
            far = node.right;
        } else {
            near = node.right;
            far = node.left;
        }

        searchRecursive(
                near,
                query,
                k,
                metric,
                best
        );

        double bestDistance =
                best.isEmpty()
                        ? Double.POSITIVE_INFINITY
                        : best.peek().distance();

        /*
         * For Euclidean distance, the splitting-plane distance
         * can be used as the pruning condition.
         *
         * For other metrics we continue searching the opposite
         * branch to preserve the correctness of the selected
         * distance calculation.
         */
        if ("euclidean".equalsIgnoreCase(metric)
                && (best.size() < k
                || Math.abs(difference) < bestDistance)) {

            searchRecursive(
                    far,
                    query,
                    k,
                    metric,
                    best
            );

        } else if (!"euclidean".equalsIgnoreCase(metric)) {

            searchRecursive(
                    far,
                    query,
                    k,
                    metric,
                    best
            );
        }
    }

    public void rebuild(List<VectorItem> items) {

        root = null;

        for (VectorItem item : items) {
            insert(item);
        }
    }

    public List<VectorItem> getItems() {

        List<VectorItem> items = new ArrayList<>();

        collectItems(root, items);

        return items;
    }

    private void collectItems(
            Node node,
            List<VectorItem> items
    ) {

        if (node == null) {
            return;
        }

        items.add(node.vector);

        collectItems(node.left, items);
        collectItems(node.right, items);
    }

    private double calculateDistance(
            float[] query,
            float[] embedding,
            String metric
    ) {

        if (metric == null) {
            metric = "euclidean";
        }

        return switch (metric.toLowerCase()) {

            case "cosine" ->
                    DistanceMetrics.cosine(
                            query,
                            embedding
                    );

            case "manhattan" ->
                    DistanceMetrics.manhattan(
                            query,
                            embedding
                    );

            case "euclidean" ->
                    DistanceMetrics.euclidean(
                            query,
                            embedding
                    );

            default ->
                    DistanceMetrics.euclidean(
                            query,
                            embedding
                    );
        };
    }

    private static class Node {

        private final VectorItem vector;
        private final int axis;

        private Node left;
        private Node right;

        private Node(
                VectorItem vector,
                int axis
        ) {

            this.vector = vector;
            this.axis = axis;
        }
    }

    public record SearchResult(
            double distance,
            int id
    ) {
    }
}