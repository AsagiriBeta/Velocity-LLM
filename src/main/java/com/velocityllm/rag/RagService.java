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

    private final Path dataDirectory;
    private final ConfigManager configManager;
    private final DocumentStore documentStore;
    private final EmbeddingService embeddingService;
    private final TfidfRetriever tfidfRetriever;
    private final EmbeddingRetriever embeddingRetriever;
    private final Logger logger;

    private boolean embeddingIndexReady;

    public RagService(
            Path dataDirectory,
            ConfigManager configManager,
            EmbeddingService embeddingService,
            Logger logger
    ) {
        this.dataDirectory = dataDirectory;
        this.configManager = configManager;
        this.documentStore = new DocumentStore(dataDirectory, configManager, logger);
        this.embeddingService = embeddingService;
        this.tfidfRetriever = new TfidfRetriever();
        this.embeddingRetriever = new EmbeddingRetriever();
        this.logger = logger;
    }

    public int reload() throws IOException {
        int chunkCount = documentStore.reload();
        embeddingIndexReady = false;

        PluginConfig config = configManager.getConfig();
        if (!config.isRagEnabled() || !shouldUseEmbedding(config)) {
            logger.info("RAG 已加载 {} 个片段（检索模式: TF-IDF）", chunkCount);
            return chunkCount;
        }

        try {
            buildEmbeddingIndex(config);
            embeddingIndexReady = true;
            logger.info("RAG 已加载 {} 个片段（检索模式: Embedding / {}）", chunkCount, config.getEmbeddingModel());
        } catch (Exception e) {
            embeddingIndexReady = false;
            logger.warn("Embedding 索引构建失败，将回退到 TF-IDF: {}", e.getMessage());
        }

        return chunkCount;
    }

    private void buildEmbeddingIndex(PluginConfig config) {
        List<DocumentChunk> chunks = documentStore.getChunks();
        if (chunks.isEmpty()) {
            return;
        }

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
        if (!config.isRagEnabled()) {
            return List.of();
        }

        List<DocumentChunk> chunks = documentStore.getChunks();
        if (chunks.isEmpty()) {
            return List.of();
        }

        List<ScoredChunk> scored = retrieveScored(query, config, chunks);
        return pickTopK(scored, config.getMaxChunks(), config.getMinScore());
    }

    private List<ScoredChunk> retrieveScored(String query, PluginConfig config, List<DocumentChunk> chunks) {
        if (shouldQueryEmbedding(config)) {
            try {
                float[] queryEmbedding = embeddingService.embed(config, query).join();
                List<ScoredChunk> scored = embeddingRetriever.score(queryEmbedding, chunks);
                if (!scored.isEmpty()) {
                    return scored;
                }
            } catch (Exception e) {
                logger.debug("Embedding 检索失败，回退 TF-IDF: {}", e.getMessage());
            }
        }

        return tfidfRetriever.score(query, chunks);
    }

    private boolean shouldUseEmbedding(PluginConfig config) {
        return config.getRetrievalMode() != PluginConfig.RetrievalMode.TFIDF
                && config.isEmbeddingEnabled()
                && config.getApiStyle() == PluginConfig.ApiStyle.OLLAMA;
    }

    private boolean shouldQueryEmbedding(PluginConfig config) {
        if (config.getRetrievalMode() == PluginConfig.RetrievalMode.TFIDF) {
            return false;
        }
        if (config.getRetrievalMode() == PluginConfig.RetrievalMode.EMBEDDING) {
            return embeddingIndexReady;
        }
        return embeddingIndexReady;
    }

    private List<DocumentChunk> pickTopK(List<ScoredChunk> scored, int topK, double minScore) {
        if (scored.isEmpty()) {
            return List.of();
        }

        double threshold = minScore;
        double maxScore = scored.getFirst().score();
        if (maxScore > 1.0) {
            threshold = maxScore * minScore;
        }

        List<DocumentChunk> result = new ArrayList<>();
        for (ScoredChunk item : scored) {
            if (result.size() >= topK) {
                break;
            }
            if (item.score() < threshold) {
                continue;
            }
            result.add(item.chunk());
        }
        return result;
    }

    public boolean isEmpty() {
        return documentStore.isEmpty();
    }

    public int getChunkCount() {
        return documentStore.getChunks().size();
    }
}
