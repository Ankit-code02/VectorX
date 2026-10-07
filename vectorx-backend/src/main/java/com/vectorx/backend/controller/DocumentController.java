package com.vectorx.backend.controller;

import com.vectorx.backend.model.DocumentItem;
import com.vectorx.backend.service.DocumentDatabase;
import com.vectorx.backend.service.OllamaClient;
import com.vectorx.backend.service.VectorXService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doc")
public class DocumentController {

    private final DocumentDatabase documentDatabase;
    private final OllamaClient ollamaClient;

    public DocumentController(VectorXService vectorXService) {
        this.documentDatabase =
                vectorXService.getDocumentDatabase();

        this.ollamaClient =
                vectorXService.getOllamaClient();
    }

    @PostMapping("/insert")
    public ResponseEntity<?> insert(
            @RequestBody InsertDocumentRequest request
    ) {
        try {
            if (request.title() == null
                    || request.text() == null
                    || request.title().isEmpty()
                    || request.text().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "error", "title and text are required"
                        ));
            }

            List<String> chunks = chunkText(request.text(), 250, 30);

            List<Integer> ids = new ArrayList<>();

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                float[] embedding = ollamaClient.embed(chunk);

                if (embedding.length == 0) {
                    return ResponseEntity.ok(
                            Map.of(
                                    "error",
                                    "Unable to generate embedding. Is Ollama running with nomic-embed-text?"
                            )
                    );
                }

                String title = request.title();

                if (chunks.size() > 1) {
                    title = request.title()
                            + " [" + (i + 1) + "/" + chunks.size() + "]";
                }

                int id = documentDatabase.insert(
                        title,
                        chunk,
                        embedding
                );

                ids.add(id);
            }

            int dims = ids.isEmpty()
                    ? 0
                    : documentDatabase.all().get(0).getEmbedding().length;

            return ResponseEntity.ok(
                    Map.of(
                            "ids", ids,
                            "chunks", chunks.size(),
                            "dims", dims
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", e.getMessage()
                    ));
        }
    }
    private List<String> chunkText(
            String text,
            int chunkWords,
            int overlapWords
    ) {
        String[] words = text.trim().split("\\s+");

        if (words.length <= chunkWords) {
            return List.of(text.trim());
        }

        List<String> chunks = new ArrayList<>();

        int step = chunkWords - overlapWords;

        for (int start = 0; start < words.length; start += step) {
            int end = Math.min(start + chunkWords, words.length);

            StringBuilder chunk = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (chunk.length() > 0) {
                    chunk.append(" ");
                }

                chunk.append(words[i]);
            }

            chunks.add(chunk.toString());

            if (end == words.length) {
                break;
            }
        }

        return chunks;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable int id) {
        boolean removed = documentDatabase.remove(id);

        return ResponseEntity.ok(
                Map.of("ok", removed)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        List<Map<String, Object>> documents = new ArrayList<>();

        for (DocumentItem document : documentDatabase.all()) {
            String preview = document.getText();

            if (preview.length() > 120) {
                preview = preview.substring(0, 120) + "…";
            }

            documents.add(
                    Map.of(
                            "id", document.getId(),
                            "title", document.getTitle(),
                            "preview", preview,
                            "words", document.getText().trim().isEmpty()
                                    ? 0
                                    : document.getText().trim().split("\\s+").length
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of("documents", documents)
        );
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(
            @RequestBody DocumentSearchRequest request
    ) {

        try {

            if (request.question() == null
                    || request.question().isBlank()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "query is required"
                                )
                        );
            }

            int k =
                    request.k() == null
                            ? 3
                            : request.k();

            float[] embedding =
                    ollamaClient.embed(
                            request.question()
                    );

            if (embedding.length == 0) {

                return ResponseEntity
                        .status(503)
                        .body(
                                Map.of(
                                        "error",
                                        "Unable to generate query embedding. Check Ollama."
                                )
                        );
            }

            var matches =
                    documentDatabase.search(
                            embedding,
                            k
                    );

            List<Map<String, Object>> contexts = new ArrayList<>();

            for (var match : matches) {
                DocumentItem document = match.document();

                contexts.add(
                        Map.of(
                                "id", document.getId(),
                                "title", document.getTitle(),
                                "distance", Math.round(match.distance() * 10000.0) / 10000.0
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "contexts", contexts
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

    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            @RequestBody AskRequest request
    ) {

        try {

            if (request.question() == null
                    || request.question().isBlank()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "question is required"
                                )
                        );
            }

            int k =
                    request.k() == null
                            ? 3
                            : request.k();

            /*
             * Convert the user's question into an embedding.
             */
            float[] embedding =
                    ollamaClient.embed(
                            request.question()
                    );

            if (embedding.length == 0) {

                return ResponseEntity
                        .status(503)
                        .body(
                                Map.of(
                                        "error",
                                        "Unable to generate question embedding. Check Ollama."
                                )
                        );
            }

            /*
             * Retrieve the relevant document chunks.
             */
            var retrieved =
                    documentDatabase.search(
                            embedding,
                            k
                    );

            /*
             * Build the context used by the generation model.
             */
            StringBuilder context =
                    new StringBuilder();

            for (var result : retrieved) {

                DocumentItem document =
                        result.document();

                context
                        .append("Title: ")
                        .append(document.getTitle())
                        .append("\n");

                context
                        .append("Text: ")
                        .append(document.getText())
                        .append("\n\n");
            }

            String prompt =
                    buildPrompt(
                            request.question(),
                            context.toString()
                    );

            String answer =
                    ollamaClient.generate(
                            prompt
                    );

            List<Map<String, Object>> sources =
                    new ArrayList<>();

            for (var result : retrieved) {

                DocumentItem document =
                        result.document();

                sources.add(
                        Map.of(
                                "id",
                                document.getId(),

                                "title",
                                document.getTitle(),

                                "distance",
                                Math.round(result.distance() * 10000.0) / 10000.0
                        )
                );
            }

            return ResponseEntity.ok(
                    Map.of(
                            "answer", answer,
                            "model", ollamaClient.genModel,
                            "contexts", sources,
                            "docCount", documentDatabase.size()
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

    private String buildPrompt(String question, String context) {
        return "You are a helpful assistant. Answer the user's question directly. Use the provided context if it contains relevant information. If it doesn't, just use your own general knowledge. IMPORTANT: Do NOT mention the 'context', 'provided text', or say things like 'the context doesn't mention'. Just answer the question naturally.\n\n"
                + "Context:\n"
                + context
                + "Question: " + question + "\n\n"
                + "Answer:";
    }
    public record InsertDocumentRequest(
            String title,
            String text
    ) {
    }

    public record DocumentSearchRequest(
            String question,
            Integer k
    ) {
    }

    public record AskRequest(
            String question,
            Integer k
    ) {
    }
}