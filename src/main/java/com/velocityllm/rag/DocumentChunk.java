package com.velocityllm.rag;

import java.util.List;

public final class DocumentChunk {

    private final String sourceFile;
    private final String content;
    private final List<String> tokens;
    private float[] embedding;

    public DocumentChunk(String sourceFile, String content, List<String> tokens) {
        this.sourceFile = sourceFile;
        this.content = content;
        this.tokens = tokens;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public boolean hasEmbedding() {
        return embedding != null && embedding.length > 0;
    }
}
