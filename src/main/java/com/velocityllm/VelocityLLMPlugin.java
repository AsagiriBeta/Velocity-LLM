package com.velocityllm;

import com.google.inject.Inject;
import com.velocityllm.ai.AIService;
import com.velocityllm.ai.ChatService;
import com.velocityllm.ai.EmbeddingService;
import com.velocityllm.command.ConsoleCommand;
import com.velocityllm.config.ConfigManager;
import com.velocityllm.history.ChatHistoryManager;
import com.velocityllm.listener.ChatListener;
import com.velocityllm.listener.DisconnectListener;
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
        version = BuildConstants.VERSION,
        description = "Velocity Ollama AI assistant with RAG",
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
        configManager = new ConfigManager(dataDirectory, logger);
        try {
            configManager.load();
            configManager.copyDefaultDocsIfMissing();
        } catch (Exception e) {
            logger.error("配置加载失败，插件无法启动", e);
            return;
        }

        chatHistoryManager = new ChatHistoryManager();
        EmbeddingService embeddingService = new EmbeddingService();
        ragService = new RagService(dataDirectory, configManager, embeddingService, logger);

        try {
            ragService.reload();
        } catch (Exception e) {
            logger.warn("文档加载失败: {}", e.getMessage());
        }

        AIService aiService = new AIService();
        chatService = new ChatService(this, aiService, ragService, chatHistoryManager, logger);

        server.getEventManager().register(this, new ChatListener(this, chatService));
        server.getEventManager().register(this, new DisconnectListener(chatHistoryManager));
        server.getCommandManager().register(ConsoleCommand.meta(this), new ConsoleCommand(this, chatService, logger));

        String ragStatus = ragService.isIndexReady()
                ? "就绪 (" + ragService.getChunkCount() + " 片段)"
                : "不可用";
        logger.info("Velocity LLM v{} 已启用，RAG: {}", BuildConstants.VERSION, ragStatus);
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

    public RagService getRagService() {
        return ragService;
    }

    public Logger getLogger() {
        return logger;
    }
}
