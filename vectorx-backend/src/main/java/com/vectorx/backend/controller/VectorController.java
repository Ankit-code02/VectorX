package com.vectorx.backend.controller;

import com.vectorx.backend.model.VectorItem;
import com.vectorx.backend.service.VectorDatabase;
import com.vectorx.backend.service.VectorXService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class VectorController {

    private final VectorDatabase vectorDatabase;

    public VectorController(VectorXService vectorXService) {
        this.vectorDatabase =
                vectorXService.getVectorDatabase();
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam("v") String vector,
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "cosine") String metric,
            @RequestParam(defaultValue = "hnsw") String algo
    ) {

        try {

            float[] query =
                    parseVector(vector);

            VectorDatabase.SearchOutput result =
                    vectorDatabase.search(
                            query,
                            k,
                            metric,
                            algo
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "results",
                            result.hits(),

                            "latencyUs",
                            result.latencyUs(),

                            "algo",
                            result.algorithm(),

                            "metric",
                            result.metric()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping(
            value = "/insert",
            consumes = "application/json"
    )
    public ResponseEntity<?> insert(
            @RequestBody InsertRequest request
    ) {

        try {

            if (request.emb() == null) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Missing embedding"
                                )
                        );
            }

            float[] embedding =
                    request.emb();

            int id =
                    vectorDatabase.insert(
                            request.meta(),
                            request.cat(),
                            embedding
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "id",
                            id
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(
            @PathVariable int id
    ) {

        boolean removed = vectorDatabase.remove(id);

        return ResponseEntity.ok(
                Map.of(
                        "ok",
                        removed
                )
        );
    }

    @GetMapping("/items")
    public List<VectorItem> items() {

        return vectorDatabase.all();
    }

    @GetMapping("/benchmark")
    public ResponseEntity<?> benchmark(
            @RequestParam("v") String vector,
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "cosine") String metric
    ) {

        try {

            float[] query =
                    parseVector(vector);

            VectorDatabase.BenchmarkOutput result =
                    vectorDatabase.benchmark(
                            query,
                            k,
                            metric
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "bruteforceUs",
                            result.bruteForceUs(),

                            "kdtreeUs",
                            result.kdTreeUs(),

                            "hnswUs",
                            result.hnswUs(),

                            "itemCount",
                            result.itemCount()
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {

        return ResponseEntity.ok(
                Map.of(
                        "count", vectorDatabase.size(),
                        "dims", vectorDatabase.getDimensions(),
                        "algorithms", List.of(
                                "bruteforce",
                                "kdtree",
                                "hnsw"
                        ),
                        "metrics", List.of(
                                "euclidean",
                                "cosine",
                                "manhattan"
                        )
                )
        );
    }

    @GetMapping("/hnsw-info")
    public ResponseEntity<?> hnswInfo() {

        VectorDatabase.GraphInfo info =
                vectorDatabase.hnswInfo();

        return ResponseEntity.ok(
                Map.of(
                        "topLayer",
                        info.topLayer(),

                        "nodeCount",
                        info.nodeCount(),

                        "nodesPerLayer",
                        info.nodesPerLayer(),

                        "edgesPerLayer",
                        info.edgesPerLayer(),

                        "nodes",
                        info.nodes(),

                        "edges",
                        info.edges()
                )
        );
    }

    private float[] parseVector(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Vector cannot be empty"
            );
        }

        String[] parts =
                value.split(",");

        float[] result =
                new float[parts.length];

        for (int i = 0;
             i < parts.length;
             i++) {

            try {

                result[i] =
                        Float.parseFloat(
                                parts[i].trim()
                        );

            } catch (NumberFormatException e) {

                throw new IllegalArgumentException(
                        "Invalid vector value: "
                                + parts[i]
                );
            }
        }

        return result;
    }

    public record InsertRequest(
            String meta,
            String cat,
            float[] emb
    ) {
    }
}