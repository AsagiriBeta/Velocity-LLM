package com.velocityllm.rag;

public record ScoredChunk(DocumentChunk chunk, double score) {
}
