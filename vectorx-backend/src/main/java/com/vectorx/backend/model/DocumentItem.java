package com.vectorx.backend.model;

public class DocumentItem {

    private int id;
    private String title;
    private String text;
    private float[] embedding;

    public DocumentItem() {
    }

    public DocumentItem(
            int id,
            String title,
            String text,
            float[] embedding
    ) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.embedding = embedding;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}