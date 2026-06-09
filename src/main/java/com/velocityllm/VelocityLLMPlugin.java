package com.velocityllm;

import com.google.inject.Inject;
import com.velocityllm.ai.AIService;
import com.velocityllm.ai.ChatService;
import com.velocityllm.ai.EmbeddingService;
import com.velocityllm.command.ConsoleCommand;
import com.velocityllm.config.ConfigManager;
import com.velocityllm.history.ChatHistoryManager;
import com.velocityllm.listener.ChatListener;
import com.velocityllm.rag.RagService;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "velocity-llm",
        name = "Velocity LLM",
        version = "1.0.0",
        description = "Velocity AI assistant with RAG support",
        authors = {"Velocity-LLM"}
)
public final class VelocityLLMPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigManager configManager;
    private ChatHistoryManager chatHistoryManager;
    private RagService ragService;
    private ChatService chatService;

    @Inject
    public VelocityLLMPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            configManager = new ConfigManager(dataDirectory, logger);
            configManager.load();
            configManager.copyDefaultDocsIfMissing();

            chatHistoryManager = new ChatHistoryManager();
            EmbeddingService embeddingService = new EmbeddingService();
            ragService = new RagService(dataDirectory, configManager, embeddingService, logger);
            ragService.reload();

            AIService aiService = new AIService();
            chatService = new ChatService(this, aiService, ragService, chatHistoryManager, logger);

            server.getEventManager().register(this, new ChatListener(this, chatService));
            server.getCommandManager().register(ConsoleCommand.meta(this), new ConsoleCommand(this, chatService, logger));

            logger.info("Velocity LLM 已启用（Velocity 3.5+）。文档片段: {}", ragService.getChunkCount());
        } catch (Exception e) {
            logger.error("Velocity LLM 启动失败", e);
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChatHistoryManager getChatHistoryManager() {
        return chatHistoryManager;
    }

    public Logger getLogger() {
        return logger;
    }
}
