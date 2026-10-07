package com.vectorx.backend.service;

public final class DemoData {

    private DemoData() {
    }

    public static void loadDemo(VectorDatabase db) {

        db.insert(
                "Linked List: nodes connected by pointers",
                "cs",
                new float[]{
                        0.90f, 0.85f, 0.72f, 0.68f,
                        0.12f, 0.08f, 0.15f, 0.10f,
                        0.05f, 0.08f, 0.06f, 0.09f,
                        0.07f, 0.11f, 0.08f, 0.06f
                }
        );

        db.insert(
                "Binary Search Tree: O(log n) search and insert",
                "cs",
                new float[]{
                        0.88f, 0.82f, 0.78f, 0.74f,
                        0.15f, 0.10f, 0.08f, 0.12f,
                        0.06f, 0.07f, 0.08f, 0.05f,
                        0.09f, 0.06f, 0.07f, 0.10f
                }
        );

        db.insert(
                "Dynamic Programming: memoization overlapping subproblems",
                "cs",
                new float[]{
                        0.82f, 0.76f, 0.88f, 0.80f,
                        0.20f, 0.18f, 0.12f, 0.09f,
                        0.07f, 0.06f, 0.08f, 0.07f,
                        0.08f, 0.09f, 0.06f, 0.07f
                }
        );

        db.insert(
                "Graph BFS and DFS: breadth and depth first traversal",
                "cs",
                new float[]{
                        0.85f, 0.80f, 0.75f, 0.82f,
                        0.18f, 0.14f, 0.10f, 0.08f,
                        0.06f, 0.09f, 0.07f, 0.06f,
                        0.10f, 0.08f, 0.09f, 0.07f
                }
        );

        db.insert(
                "Hash Table: O(1) lookup with collision chaining",
                "cs",
                new float[]{
                        0.87f, 0.78f, 0.70f, 0.76f,
                        0.13f, 0.11f, 0.09f, 0.14f,
                        0.08f, 0.07f, 0.06f, 0.08f,
                        0.07f, 0.10f, 0.08f, 0.09f
                }
        );

        db.insert(
                "Calculus: derivatives integrals and limits",
                "math",
                new float[]{
                        0.12f, 0.15f, 0.18f, 0.10f,
                        0.91f, 0.86f, 0.78f, 0.72f,
                        0.08f, 0.06f, 0.07f, 0.09f,
                        0.07f, 0.08f, 0.06f, 0.10f
                }
        );

        db.insert(
                "Linear Algebra: matrices eigenvalues eigenvectors",
                "math",
                new float[]{
                        0.20f, 0.18f, 0.15f, 0.12f,
                        0.88f, 0.90f, 0.82f, 0.76f,
                        0.09f, 0.07f, 0.08f, 0.06f,
                        0.10f, 0.07f, 0.08f, 0.09f
                }
        );

        db.insert(
                "Probability: distributions random variables Bayes theorem",
                "math",
                new float[]{
                        0.15f, 0.12f, 0.20f, 0.18f,
                        0.84f, 0.80f, 0.88f, 0.82f,
                        0.07f, 0.08f, 0.06f, 0.10f,
                        0.09f, 0.06f, 0.09f, 0.08f
                }
        );

        db.insert(
                "Number Theory: primes modular arithmetic RSA cryptography",
                "math",
                new float[]{
                        0.22f, 0.16f, 0.14f, 0.20f,
                        0.80f, 0.85f, 0.76f, 0.90f,
                        0.08f, 0.09f, 0.07f, 0.06f,
                        0.08f, 0.10f, 0.07f, 0.06f
                }
        );

        db.insert(
                "Combinatorics: permutations combinations generating functions",
                "math",
                new float[]{
                        0.18f, 0.20f, 0.16f, 0.14f,
                        0.86f, 0.78f, 0.84f, 0.80f,
                        0.06f, 0.07f, 0.09f, 0.08f,
                        0.06f, 0.09f, 0.10f, 0.07f
                }
        );

        db.insert(
                "Neapolitan Pizza: wood-fired dough San Marzano tomatoes",
                "food",
                new float[]{
                        0.08f, 0.06f, 0.09f, 0.07f,
                        0.07f, 0.08f, 0.06f, 0.09f,
                        0.90f, 0.86f, 0.78f, 0.72f,
                        0.08f, 0.06f, 0.09f, 0.07f
                }
        );

        db.insert(
                "Sushi: vinegared rice raw fish and nori rolls",
                "food",
                new float[]{
                        0.06f, 0.08f, 0.07f, 0.09f,
                        0.09f, 0.06f, 0.08f, 0.07f,
                        0.86f, 0.90f, 0.82f, 0.76f,
                        0.07f, 0.09f, 0.06f, 0.08f
                }
        );

        db.insert(
                "Ramen: noodle soup with chashu pork and soft-boiled eggs",
                "food",
                new float[]{
                        0.09f, 0.07f, 0.06f, 0.08f,
                        0.08f, 0.09f, 0.07f, 0.06f,
                        0.82f, 0.78f, 0.90f, 0.84f,
                        0.09f, 0.07f, 0.08f, 0.06f
                }
        );

        db.insert(
                "Tacos: corn tortillas with carnitas salsa and cilantro",
                "food",
                new float[]{
                        0.07f, 0.09f, 0.08f, 0.06f,
                        0.06f, 0.07f, 0.09f, 0.08f,
                        0.78f, 0.82f, 0.86f, 0.90f,
                        0.06f, 0.08f, 0.07f, 0.09f
                }
        );

        db.insert(
                "Croissant: laminated pastry with buttery flaky layers",
                "food",
                new float[]{
                        0.06f, 0.07f, 0.10f, 0.09f,
                        0.10f, 0.06f, 0.07f, 0.10f,
                        0.85f, 0.80f, 0.76f, 0.82f,
                        0.09f, 0.07f, 0.10f, 0.06f
                }
        );

        db.insert(
                "Basketball: fast-paced shooting dribbling slam dunks",
                "sports",
                new float[]{
                        0.09f, 0.07f, 0.08f, 0.10f,
                        0.08f, 0.09f, 0.07f, 0.06f,
                        0.08f, 0.07f, 0.09f, 0.06f,
                        0.91f, 0.85f, 0.78f, 0.72f
                }
        );

        db.insert(
                "Football: tackles touchdowns field goals and strategy",
                "sports",
                new float[]{
                        0.07f, 0.09f, 0.06f, 0.08f,
                        0.09f, 0.07f, 0.10f, 0.08f,
                        0.07f, 0.09f, 0.08f, 0.07f,
                        0.87f, 0.89f, 0.82f, 0.76f
                }
        );

        db.insert(
                "Tennis: racket volleys groundstrokes and Wimbledon serves",
                "sports",
                new float[]{
                        0.08f, 0.06f, 0.09f, 0.07f,
                        0.07f, 0.08f, 0.06f, 0.09f,
                        0.09f, 0.06f, 0.07f, 0.08f,
                        0.83f, 0.80f, 0.88f, 0.82f
                }
        );

        db.insert(
                "Chess: openings endgames tactics strategic board game",
                "sports",
                new float[]{
                        0.25f, 0.20f, 0.22f, 0.18f,
                        0.22f, 0.18f, 0.20f, 0.15f,
                        0.06f, 0.08f, 0.07f, 0.09f,
                        0.80f, 0.84f, 0.78f, 0.90f
                }
        );

        db.insert(
                "Swimming: butterfly freestyle backstroke Olympic competition",
                "sports",
                new float[]{
                        0.06f, 0.08f, 0.07f, 0.09f,
                        0.08f, 0.06f, 0.09f, 0.07f,
                        0.10f, 0.08f, 0.06f, 0.07f,
                        0.85f, 0.82f, 0.86f, 0.80f
                }
        );
    }
}