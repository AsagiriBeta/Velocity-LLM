package com.velocityllm.config;

import com.velocityllm.ai.OllamaUrls;
import org.slf4j.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private PluginConfig config;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.config = new PluginConfig();
    }

    public PluginConfig getConfig() {
        return config;
    }

    public void load() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }

        Path configFile = dataDirectory.resolve("config.toml");
        if (!Files.exists(configFile)) {
            saveDefaultConfig(configFile);
        }

        TomlParseResult parsed = Toml.parse(configFile);
        if (parsed.hasErrors()) {
            parsed.errors().forEach(error -> logger.error("config.toml 解析错误: {}", error.toString()));
            throw new IOException("config.toml 解析失败");
        }

        PluginConfig loaded = new PluginConfig();
        TomlTable ai = parsed.getTable("ai");
        if (ai != null) {
            loaded.setAiEnabled(ai.getBoolean("enabled", loaded::isAiEnabled));
            loaded.setBaseUrl(resolveBaseUrl(ai, loaded.getBaseUrl()));
            loaded.setModel(ai.getString("model", loaded::getModel));
            loaded.setUseChatApi(resolveUseChatApi(ai, loaded.isUseChatApi()));
            loaded.setTemperature(ai.getDouble("temperature", loaded::getTemperature));
            loaded.setTimeoutSeconds((int) ai.getLong("timeout-seconds", loaded::getTimeoutSeconds));
        }

        TomlTable chat = parsed.getTable("chat");
        if (chat != null) {
            loaded.setTriggerEnabled(chat.getBoolean("trigger-enabled", loaded::isTriggerEnabled));
            loaded.setTriggerPrefixes(readStringList(chat, "trigger-prefixes", loaded.getTriggerPrefixes()));
            loaded.setCooldownSeconds((int) chat.getLong("cooldown-seconds", loaded::getCooldownSeconds));
            loaded.setMaxHistory((int) chat.getLong("max-history", loaded::getMaxHistory));
        }

        TomlTable rag = parsed.getTable("rag");
        if (rag != null) {
            loaded.setRagEnabled(rag.getBoolean("enabled", loaded::isRagEnabled));
            loaded.setDocsFolder(rag.getString("docs-folder", loaded::getDocsFolder));
            loaded.setMaxChunks((int) rag.getLong("max-chunks", loaded::getMaxChunks));
            loaded.setChunkSize((int) rag.getLong("chunk-size", loaded::getChunkSize));
            loaded.setChunkOverlap((int) rag.getLong("chunk-overlap", loaded::getChunkOverlap));
            loaded.setEmbeddingModel(rag.getString("embedding-model", loaded::getEmbeddingModel));
            loaded.setMinScore(rag.getDouble("min-score", loaded::getMinScore));
        }

        TomlTable response = parsed.getTable("response");
        if (response != null) {
            loaded.setMaxResponseLength((int) response.getLong("max-length", loaded::getMaxResponseLength));
            loaded.setSystemPrompt(response.getString("system-prompt", loaded::getSystemPrompt));
            loaded.setResponseVisibility(parseResponseVisibility(response.getString("visibility"), loaded.getResponseVisibility()));
            loaded.setShowPlayerMessages(resolveShowPlayerMessages(response, loaded.isShowPlayerMessages()));
        }

        TomlTable messages = parsed.getTable("messages");
        if (messages != null) {
            loaded.setMessagePrefix(messages.getString("prefix", loaded::getMessagePrefix));
            loaded.setMessageThinking(messages.getString("thinking", loaded::getMessageThinking));
            loaded.setMessageCooldown(messages.getString("cooldown", loaded::getMessageCooldown));
            loaded.setMessageEmptyInput(resolveEmptyInput(messages, loaded.getMessageEmptyInput()));
            loaded.setMessageError(messages.getString("error", loaded::getMessageError));
            loaded.setMessageDisabled(messages.getString("disabled", loaded::getMessageDisabled));
        }

        this.config = loaded;
        logger.info("配置已加载: model={}, rag={}", loaded.getModel(), loaded.isRagEnabled());
    }

    private String resolveBaseUrl(TomlTable ai, String defaultValue) {
        String baseUrl = ai.getString("base-url");
        if (baseUrl != null && !baseUrl.isBlank()) {
            return OllamaUrls.normalizeBaseUrl(baseUrl);
        }

        String legacyApiUrl = ai.getString("api-url");
        if (legacyApiUrl != null && !legacyApiUrl.isBlank()) {
            return OllamaUrls.normalizeBaseUrl(legacyApiUrl);
        }

        return defaultValue;
    }

    private boolean resolveUseChatApi(TomlTable ai, boolean defaultValue) {
        if (ai.contains("use-chat-api")) {
            return ai.getBoolean("use-chat-api");
        }
        if (ai.contains("messages-format")) {
            return ai.getBoolean("messages-format");
        }
        String legacyApiUrl = ai.getString("api-url");
        if (legacyApiUrl != null) {
            return legacyApiUrl.contains("/api/chat");
        }
        return defaultValue;
    }

    private boolean resolveShowPlayerMessages(TomlTable response, boolean defaultValue) {
        if (response.contains("show-player-messages")) {
            return response.getBoolean("show-player-messages");
        }
        if (response.contains("show-question")) {
            return response.getBoolean("show-question");
        }
        return defaultValue;
    }

    private String resolveEmptyInput(TomlTable messages, String defaultValue) {
        String value = messages.getString("empty-input");
        if (value != null && !value.isBlank()) {
            return value;
        }
        String legacy = messages.getString("empty-question");
        return legacy != null && !legacy.isBlank() ? legacy : defaultValue;
    }

    private PluginConfig.ResponseVisibility parseResponseVisibility(String value, PluginConfig.ResponseVisibility defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "private" -> PluginConfig.ResponseVisibility.PRIVATE;
            default -> PluginConfig.ResponseVisibility.SAME_SERVER;
        };
    }

    private List<String> readStringList(TomlTable table, String key, List<String> defaultValue) {
        TomlArray array = table.getArray(key);
        if (array == null) {
            return defaultValue;
        }
        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            values.add(array.getString(i));
        }
        return values.isEmpty() ? defaultValue : values;
    }

    private void saveDefaultConfig(Path configFile) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.toml")) {
            if (in == null) {
                throw new IOException("默认 config.toml 未找到");
            }
            Files.copy(in, configFile);
        }
    }

    public void copyDefaultDocsIfMissing() throws IOException {
        Path docsDir = dataDirectory.resolve(config.getDocsFolder());
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
        }

        Path exampleDoc = docsDir.resolve("example-server-guide.md");
        if (!Files.exists(exampleDoc)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("docs/example-server-guide.md")) {
                if (in != null) {
                    Files.copy(in, exampleDoc);
                    logger.info("已创建示例文档: {}", exampleDoc);
                }
            }
        }
    }
}
