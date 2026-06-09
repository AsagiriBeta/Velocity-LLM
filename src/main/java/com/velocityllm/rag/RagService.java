package com.velocityllm.rag;

import com.velocityllm.ai.EmbeddingService;
import com.velocityllm.config.ConfigManager;
import com.velocityllm.config.PluginConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RagService {

    private final DocumentStore documentStore;
    private final EmbeddingService embeddingService;
    private final EmbeddingRetriever embeddingRetriever;
    private final ConfigManager configManager;
    private final Logger logger;

    private boolean indexReady;

    public RagService(
            Path dataDirectory,
            ConfigManager configManager,
            EmbeddingService embeddingService,
            Logger logger
    ) {
        this.configManager = configManager;
        this.documentStore = new DocumentStore(dataDirectory, configManager, logger);
        this.embeddingService = embeddingService;
        this.embeddingRetriever = new EmbeddingRetriever();
        this.logger = logger;
    }

    public int reload() throws IOException {
        int chunkCount = documentStore.reload();
        indexReady = false;

        PluginConfig config = configManager.getConfig();
        if (!config.isRagEnabled() || chunkCount == 0) {
            return chunkCount;
        }

        try {
            buildEmbeddingIndex(config);
            indexReady = true;
            logger.info("RAG 索引就绪: {} 个片段 ({})", chunkCount, config.getEmbeddingModel());
        } catch (Exception e) {
            logger.error("RAG 索引构建失败，AI 对话仍可用但暂无文档检索: {}", e.getMessage());
        }

        return chunkCount;
    }

    private void buildEmbeddingIndex(PluginConfig config) {
        List<DocumentChunk> chunks = documentStore.getChunks();
        List<String> texts = chunks.stream().map(DocumentChunk::getContent).toList();
        List<float[]> vectors = embeddingService.embedBatch(config, texts).join();

        if (vectors.size() != chunks.size()) {
            throw new IllegalStateException("embedding 数量与文档片段不一致");
        }

        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setEmbedding(vectors.get(i));
        }
    }

    public List<DocumentChunk> retrieve(String query) {
        PluginConfig config = configManager.getConfig();
        if (!config.isRagEnabled() || !indexReady) {
            return List.of();
        }

        List<DocumentChunk> chunks = documentStore.getChunks();
        if (chunks.isEmpty() || query == null || query.isBlank()) {
            return List.of();
        }

        float[] queryEmbedding = embeddingService.embed(config, query).join();
        List<ScoredChunk> scored = embeddingRetriever.score(queryEmbedding, chunks);
        return pickTopK(scored, config.getMaxChunks(), config.getMinScore());
    }

    private List<DocumentChunk> pickTopK(List<ScoredChunk> scored, int topK, double minScore) {
        List<DocumentChunk> result = new ArrayList<>();
        for (ScoredChunk item : scored) {
            if (result.size() >= topK) {
                break;
            }
            if (item.score() < minScore) {
                continue;
            }
            result.add(item.chunk());
        }
        return result;
    }

    public boolean isIndexReady() {
        return indexReady;
    }

    public boolean isEmpty() {
        return documentStore.isEmpty();
    }

    public int getChunkCount() {
        return documentStore.getChunks().size();
    }
}
