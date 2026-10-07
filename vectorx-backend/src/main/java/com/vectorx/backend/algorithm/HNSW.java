package com.vectorx.backend.algorithm;

import com.vectorx.backend.model.VectorItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class HNSW {

    private static class Node {

        private final VectorItem item;
        private final int maxLayer;

        private final List<Set<Integer>> neighbors;

        private Node(VectorItem item, int maxLayer) {
            this.item = item;
            this.maxLayer = maxLayer;

            this.neighbors = new ArrayList<>();

            for (int i = 0; i <= maxLayer; i++) {
                this.neighbors.add(new HashSet<>());
            }
        }
    }

    private final Map<Integer, Node> graph =
            new HashMap<>();

    private final int dimensions;
    private final int M;
    private final int M0;
    private final int efBuild;

    private final double mL;

    /*
     * Java Random is used for the same deterministic
     * seed value as the original implementation.
     */
    private final Random random =
            new Random(42);

    private int topLayer = -1;
    private int entryPoint = -1;

    public HNSW(
            int dimensions,
            int M,
            int efBuild
    ) {

        this.dimensions = dimensions;
        this.M = M;
        this.M0 = 2 * M;
        this.efBuild = efBuild;

        this.mL =
                1.0 / Math.log(M);
    }

    public void insert(VectorItem item) {

        insert(
                item,
                "cosine"
        );
    }

    public void insert(
            VectorItem item,
            String metric
    ) {

        if (item == null
                || item.getEmbedding() == null
                || item.getEmbedding().length != dimensions) {

            throw new IllegalArgumentException(
                    "Invalid vector dimension"
            );
        }

        int id =
                item.getId();

        if (graph.containsKey(id)) {
            return;
        }

        int level =
                randomLevel();

        Node node =
                new Node(
                        item,
                        level
                );

        graph.put(
                id,
                node
        );

        if (entryPoint == -1) {

            entryPoint = id;
            topLayer = level;

            return;
        }

        int currentEntry =
                entryPoint;

        /*
         * Search through layers above
         * the new node's level.
         */
        for (int currentLayer = topLayer;
             currentLayer > level;
             currentLayer--) {

            List<SearchResult> result =
                    searchLayer(
                            item.getEmbedding(),
                            currentEntry,
                            1,
                            currentLayer,
                            metric
                    );

            if (!result.isEmpty()) {
                currentEntry =
                        result.get(0).id();
            }
        }

        /*
         * Connect the new node from its
         * highest applicable layer down to 0.
         */
        for (int currentLayer =
             Math.min(topLayer, level);
             currentLayer >= 0;
             currentLayer--) {

            List<SearchResult> candidates =
                    searchLayer(
                            item.getEmbedding(),
                            currentEntry,
                            efBuild,
                            currentLayer,
                            metric
                    );

            int maxNeighbors =
                    currentLayer == 0
                            ? M0
                            : M;

            List<Integer> selected =
                    selectNeighbors(
                            candidates,
                            maxNeighbors
                    );

            /*
             * Store selected neighbors for
             * the new node.
             */
            node.neighbors
                    .get(currentLayer)
                    .addAll(selected);

            /*
             * Add the new node to each neighbor.
             */
            for (Integer neighborId : selected) {

                Node neighbor =
                        graph.get(neighborId);

                if (neighbor == null) {
                    continue;
                }

                if (neighbor.neighbors.size()
                        <= currentLayer) {

                    while (neighbor.neighbors.size()
                            <= currentLayer) {

                        neighbor.neighbors.add(
                                new HashSet<>()
                        );
                    }
                }

                Set<Integer> connections =
                        neighbor.neighbors
                                .get(currentLayer);

                connections.add(id);

                /*
                 * Prune neighbor connections
                 * when the maximum is exceeded.
                 */
                if (connections.size()
                        > maxNeighbors) {

                    List<SearchResult> distances =
                            new ArrayList<>();

                    for (Integer candidateId
                            : connections) {

                        Node candidate =
                                graph.get(candidateId);

                        if (candidate == null) {
                            continue;
                        }

                        double distance =
                                calculateDistance(
                                        neighbor.item
                                                .getEmbedding(),
                                        candidate.item
                                                .getEmbedding(),
                                        metric
                                );

                        distances.add(
                                new SearchResult(
                                        distance,
                                        candidateId
                                )
                        );
                    }

                    distances.sort(
                            Comparator.comparingDouble(
                                    SearchResult::distance
                            )
                    );

                    connections.clear();

                    for (int i = 0;
                         i < Math.min(
                                 maxNeighbors,
                                 distances.size()
                         );
                         i++) {

                        connections.add(
                                distances
                                        .get(i)
                                        .id()
                        );
                    }
                }
            }

            if (!candidates.isEmpty()) {
                currentEntry =
                        candidates.get(0).id();
            }
        }

        /*
         * Higher-level node becomes the new
         * entry point.
         */
        if (level > topLayer) {

            topLayer = level;
            entryPoint = id;
        }
    }

    public List<SearchResult> knn(
            float[] query,
            int k,
            String metric
    ) {

        if (query == null
                || query.length != dimensions
                || k <= 0
                || entryPoint == -1) {

            return new ArrayList<>();
        }

        int currentEntry =
                entryPoint;

        /*
         * Greedy search through upper layers.
         */
        for (int currentLayer = topLayer;
             currentLayer > 0;
             currentLayer--) {

            List<SearchResult> result =
                    searchLayer(
                            query,
                            currentEntry,
                            1,
                            currentLayer,
                            metric
                    );

            if (!result.isEmpty()) {
                currentEntry =
                        result.get(0).id();
            }
        }

        /*
         * Original VectorX uses ef = 50
         * for HNSW queries.
         */
        List<SearchResult> results =
                searchLayer(
                        query,
                        currentEntry,
                        Math.max(50, k),
                        0,
                        metric
                );

        if (results.size() > k) {

            return new ArrayList<>(
                    results.subList(0, k)
            );
        }

        return results;
    }

    private List<SearchResult> searchLayer(
            float[] query,
            int entry,
            int ef,
            int layer,
            String metric
    ) {

        Node entryNode =
                graph.get(entry);

        if (entryNode == null) {
            return new ArrayList<>();
        }

        /*
         * Candidates:
         * smallest distance first.
         */
        PriorityQueue<SearchResult> candidates =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                SearchResult::distance
                        )
                );

        /*
         * Found:
         * largest distance first so the
         * worst result is always at the top.
         */
        PriorityQueue<SearchResult> found =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                SearchResult::distance
                        ).reversed()
                );

        Set<Integer> visited =
                new HashSet<>();

        double initialDistance =
                calculateDistance(
                        query,
                        entryNode.item.getEmbedding(),
                        metric
                );

        SearchResult initial =
                new SearchResult(
                        initialDistance,
                        entry
                );

        candidates.offer(initial);
        found.offer(initial);

        visited.add(entry);

        while (!candidates.isEmpty()) {

            SearchResult current =
                    candidates.poll();

            SearchResult worst =
                    found.peek();

            if (found.size() >= ef
                    && worst != null
                    && current.distance()
                    > worst.distance()) {

                break;
            }

            Node currentNode =
                    graph.get(current.id());

            if (currentNode == null) {
                continue;
            }

            if (layer >= currentNode.neighbors.size()) {
                continue;
            }

            Set<Integer> neighbors =
                    currentNode.neighbors
                            .get(layer);

            for (Integer neighborId : neighbors) {

                if (visited.contains(neighborId)) {
                    continue;
                }

                Node neighbor =
                        graph.get(neighborId);

                if (neighbor == null) {
                    continue;
                }

                visited.add(neighborId);

                double distance =
                        calculateDistance(
                                query,
                                neighbor.item
                                        .getEmbedding(),
                                metric
                        );

                SearchResult result =
                        new SearchResult(
                                distance,
                                neighborId
                        );

                if (found.size() < ef) {

                    candidates.offer(result);
                    found.offer(result);

                } else {

                    SearchResult worstResult =
                            found.peek();

                    if (worstResult != null
                            && distance
                            < worstResult.distance()) {

                        candidates.offer(result);

                        found.poll();
                        found.offer(result);
                    }
                }
            }
        }

        List<SearchResult> results =
                new ArrayList<>(found);

        results.sort(
                Comparator.comparingDouble(
                        SearchResult::distance
                )
        );

        return results;
    }

    private List<Integer> selectNeighbors(
            List<SearchResult> candidates,
            int maxNeighbors
    ) {

        List<Integer> result =
                new ArrayList<>();

        for (int i = 0;
             i < Math.min(
                     candidates.size(),
                     maxNeighbors
             );
             i++) {

            result.add(
                    candidates
                            .get(i)
                            .id()
            );
        }

        return result;
    }

    public void remove(int id) {

        Node removed =
                graph.get(id);

        if (removed == null) {
            return;
        }

        /*
         * Remove this node from all
         * neighbor lists.
         */
        for (Node node : graph.values()) {

            for (Set<Integer> neighbors
                    : node.neighbors) {

                neighbors.remove(id);
            }
        }

        /*
         * Match the original C++ behavior:
         * if the entry point is removed,
         * select another existing node.
         *
         * Do NOT rebuild the whole graph.
         */
        if (entryPoint == id) {

            entryPoint = -1;

            for (Integer nodeId : graph.keySet()) {

                if (nodeId != id) {

                    entryPoint = nodeId;
                    break;
                }
            }
        }

        graph.remove(id);

        if (graph.isEmpty()) {

            entryPoint = -1;
            topLayer = -1;
        }
    }

    private int randomLevel() {

        double value =
                random.nextDouble();

        /*
         * Prevent log(0).
         */
        if (value <= 0.0) {
            value =
                    Double.MIN_VALUE;
        }

        return (int) Math.floor(
                -Math.log(value) * mL
        );
    }

    private double calculateDistance(
            float[] a,
            float[] b,
            String metric
    ) {

        if (metric == null) {
            metric = "cosine";
        }

        return switch (metric.toLowerCase()) {

            case "cosine" ->
                    DistanceMetrics.cosine(
                            a,
                            b
                    );

            case "manhattan" ->
                    DistanceMetrics.manhattan(
                            a,
                            b
                    );

            case "euclidean" ->
                    DistanceMetrics.euclidean(
                            a,
                            b
                    );

            default ->
                    DistanceMetrics.euclidean(
                            a,
                            b
                    );
        };
    }

    public int size() {
        return graph.size();
    }

    public int getMaxLevel() {
        return topLayer;
    }

    public int getM() {
        return M;
    }

    public int getEfConstruction() {
        return efBuild;
    }

    public Map<Integer, Integer> getNodesPerLayer() {

        Map<Integer, Integer> result =
                new HashMap<>();

        int maxLayer =
                Math.max(
                        topLayer + 1,
                        1
                );

        for (Node node : graph.values()) {

            for (int layer = 0;
                 layer <= node.maxLayer
                         && layer < maxLayer;
                 layer++) {

                result.merge(
                        layer,
                        1,
                        Integer::sum
                );
            }
        }

        return result;
    }

    public Map<Integer, Integer> getEdgesPerLayer() {

        Map<Integer, Integer> result =
                new HashMap<>();

        int maxLayer =
                Math.max(
                        topLayer + 1,
                        1
                );

        for (Node node : graph.values()) {

            for (int layer = 0;
                 layer < node.neighbors.size()
                         && layer < maxLayer;
                 layer++) {

                for (Integer neighborId
                        : node.neighbors
                        .get(layer)) {

                    Node neighbor =
                            graph.get(neighborId);

                    if (neighbor == null) {
                        continue;
                    }

                    /*
                     * Count each graph edge once,
                     * matching the C++ getInfo()
                     * behavior.
                     */
                    if (node.item.getId()
                            < neighborId) {

                        result.merge(
                                layer,
                                1,
                                Integer::sum
                        );
                    }
                }
            }
        }

        return result;
    }

    public List<GraphNode> getGraphNodes() {

        List<GraphNode> result =
                new ArrayList<>();

        for (Node node : graph.values()) {

            result.add(
                    new GraphNode(
                            node.item.getId(),
                            node.item.getMetadata(),
                            node.item.getCategory(),
                            node.maxLayer
                    )
            );
        }

        return result;
    }

    public List<GraphEdge> getGraphEdges() {

        List<GraphEdge> result =
                new ArrayList<>();

        for (Node node : graph.values()) {

            for (int layer = 0;
                 layer < node.neighbors.size();
                 layer++) {

                for (Integer destination
                        : node.neighbors.get(layer)) {

                    if (node.item.getId()
                            < destination) {

                        result.add(
                                new GraphEdge(
                                        node.item.getId(),
                                        destination,
                                        layer
                                )
                        );
                    }
                }
            }
        }

        return result;
    }

    public List<VectorItem> getItems() {

        List<VectorItem> result =
                new ArrayList<>();

        for (Node node : graph.values()) {
            result.add(node.item);
        }

        return result;
    }

    public record SearchResult(
            double distance,
            int id
    ) {
    }

    public record GraphNode(
            int id,
            String metadata,
            String category,
            int maxLyr
    ) {
    }

    public record GraphEdge(
            int src,
            int dst,
            int lyr
    ) {
    }
}