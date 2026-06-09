package com.velocityllm.rag;

import com.velocityllm.config.ConfigManager;
import com.velocityllm.config.PluginConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class DocumentStore {

    private final Path dataDirectory;
    private final ConfigManager configManager;
    private final Logger logger;
    private final List<DocumentChunk> chunks = new ArrayList<>();

    public DocumentStore(Path dataDirectory, ConfigManager configManager, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.configManager = configManager;
        this.logger = logger;
    }

    public int reload() throws IOException {
        chunks.clear();
        PluginConfig config = configManager.getConfig();
        Path docsDir = dataDirectory.resolve(config.getDocsFolder());
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
            return 0;
        }

        try (Stream<Path> paths = Files.walk(docsDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .forEach(this::loadFile);
        }

        logger.info("RAG 文档已加载: {} 个片段", chunks.size());
        return chunks.size();
    }

    private boolean isSupportedFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".txt");
    }

    private void loadFile(Path file) {
        try {
            PluginConfig config = configManager.getConfig();
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String source = dataDirectory.resolve(config.getDocsFolder()).relativize(file).toString();
            chunks.addAll(TextChunker.chunk(
                    source,
                    content,
                    config.getChunkSize(),
                    config.getChunkOverlap()
            ));
        } catch (IOException e) {
            logger.warn("无法读取文档 {}: {}", file, e.getMessage());
        }
    }

    public List<DocumentChunk> getChunks() {
        return List.copyOf(chunks);
    }

    public boolean isEmpty() {
        return chunks.isEmpty();
    }
}
