package com.velocityllm.rag;

import com.velocityllm.util.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EmbeddingRetriever {

    public List<ScoredChunk> score(float[] queryEmbedding, List<DocumentChunk> chunks) {
        if (queryEmbedding == null || queryEmbedding.length == 0 || chunks.isEmpty()) {
            return List.of();
        }

        List<ScoredChunk> scored = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            if (chunk.getEmbedding() == null) {
                continue;
            }
            double similarity = MathUtil.cosineSimilarity(queryEmbedding, chunk.getEmbedding());
            if (similarity > 0) {
                scored.add(new ScoredChunk(chunk, similarity));
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return scored;
    }
}
