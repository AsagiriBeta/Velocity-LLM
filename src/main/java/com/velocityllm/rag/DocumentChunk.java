package com.velocityllm.rag;

import java.util.List;

public final class DocumentChunk {

    private final String sourceFile;
    private final String content;
    private final List<String> tokens;

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
}
