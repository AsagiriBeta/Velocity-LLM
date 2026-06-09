package com.velocityllm.rag;

public final class DocumentChunk {

    private final String sourceFile;
    private final String content;
    private float[] embedding;

    public DocumentChunk(String sourceFile, String content) {
        this.sourceFile = sourceFile;
        this.content = content;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getContent() {
        return content;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
