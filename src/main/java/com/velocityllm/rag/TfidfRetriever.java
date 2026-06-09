package com.velocityllm.rag;

import com.velocityllm.util.TextUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TfidfRetriever {

    public List<ScoredChunk> score(String query, List<DocumentChunk> chunks) {
        if (chunks.isEmpty() || query == null || query.isBlank()) {
            return List.of();
        }

        List<String> queryTokens = TextUtil.tokenize(query);
        if (queryTokens.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> documentFrequency = new HashMap<>();
        for (DocumentChunk chunk : chunks) {
            Set<String> unique = new HashSet<>(chunk.getTokens());
            for (String token : unique) {
                documentFrequency.merge(token, 1, Integer::sum);
            }
        }

        int totalDocuments = chunks.size();
        List<ScoredChunk> scored = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            double score = scoreChunk(chunk, queryTokens, documentFrequency, totalDocuments);
            if (score > 0) {
                scored.add(new ScoredChunk(chunk, score));
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return scored;
    }

    private double scoreChunk(
            DocumentChunk chunk,
            List<String> queryTokens,
            Map<String, Integer> documentFrequency,
            int totalDocuments
    ) {
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : chunk.getTokens()) {
            termFrequency.merge(token, 1, Integer::sum);
        }

        double score = 0;
        for (String token : queryTokens) {
            int tf = termFrequency.getOrDefault(token, 0);
            if (tf == 0) {
                continue;
            }
            int df = documentFrequency.getOrDefault(token, 0);
            double idf = Math.log((totalDocuments + 1.0) / (df + 1.0)) + 1.0;
            score += (1.0 + Math.log(tf)) * idf;
        }
        return score;
    }
}
