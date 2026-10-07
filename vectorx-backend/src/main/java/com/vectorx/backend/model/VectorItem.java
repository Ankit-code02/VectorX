package com.vectorx.backend.model;

public class VectorItem {

    private int id;
    private String metadata;
    private String category;
    private float[] embedding;

    public VectorItem() {
    }

    public VectorItem(
            int id,
            String metadata,
            String category,
            float[] embedding
    ) {
        this.id = id;
        this.metadata = metadata;
        this.category = category;
        this.embedding = embedding;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}