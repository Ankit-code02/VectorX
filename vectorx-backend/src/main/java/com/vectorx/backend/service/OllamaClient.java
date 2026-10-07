package com.vectorx.backend.service;

import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

public class OllamaClient {

    private final RestClient client;

    public String embedModel = "nomic-embed-text";
    public String genModel = "llama3.2";

    public OllamaClient() {

        this.client = RestClient
                .builder()
                .baseUrl("http://127.0.0.1:11434")
                .build();
    }

    public boolean isAvailable() {

        try {

            client.get()
                    .uri("/api/tags")
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    public float[] embed(String text) {

        try {

            Map<String, Object> request =
                    Map.of(
                            "model",
                            embedModel,

                            "prompt",
                            text
                    );

            Map<?, ?> response =
                    client.post()
                            .uri("/api/embeddings")
                            .body(request)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {
                return new float[0];
            }

            Object embedding =
                    response.get("embedding");

            if (!(embedding instanceof List<?> values)) {
                return new float[0];
            }

            float[] result =
                    new float[values.size()];

            for (int i = 0;
                 i < values.size();
                 i++) {

                Object value =
                        values.get(i);

                if (value instanceof Number number) {
                    result[i] =
                            number.floatValue();
                }
            }

            return result;

        } catch (Exception e) {

            return new float[0];
        }
    }

    public String generate(String prompt) {

        try {

            Map<String, Object> request =
                    Map.of(
                            "model",
                            genModel,

                            "prompt",
                            prompt,

                            "stream",
                            false
                    );

            Map<?, ?> response =
                    client.post()
                            .uri("/api/generate")
                            .body(request)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {

                return "ERROR: Ollama unavailable. Run: ollama serve";
            }

            Object result =
                    response.get("response");

            if (result == null) {
                return "";
            }

            return result.toString();

        } catch (Exception e) {

            return "ERROR: Ollama unavailable. Run: ollama serve";
        }
    }
}