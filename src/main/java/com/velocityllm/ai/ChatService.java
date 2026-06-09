package com.velocityllm.ai;

import com.velocityllm.config.PluginConfig;
import com.velocityllm.rag.DocumentChunk;
import com.velocityllm.rag.DocumentStore;
import com.velocityllm.rag.KnowledgeRetriever;
import com.velocityllm.util.MessageUtil;
import com.velocityllm.util.TextUtil;
import com.velocityllm.VelocityLLMPlugin;
import com.velocityllm.history.ChatHistoryManager;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatService {

    private final VelocityLLMPlugin plugin;
    private final AIService aiService;
    private final DocumentStore documentStore;
    private final KnowledgeRetriever knowledgeRetriever;
    private final ChatHistoryManager historyManager;
    private final Logger logger;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public ChatService(
            VelocityLLMPlugin plugin,
            AIService aiService,
            DocumentStore documentStore,
            KnowledgeRetriever knowledgeRetriever,
            ChatHistoryManager historyManager,
            Logger logger
    ) {
        this.plugin = plugin;
        this.aiService = aiService;
        this.documentStore = documentStore;
        this.knowledgeRetriever = knowledgeRetriever;
        this.historyManager = historyManager;
        this.logger = logger;
    }

    public void ask(Player player, String question) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        if (!config.isAiEnabled()) {
            player.sendMessage(MessageUtil.parse(config.getMessageDisabled()));
            return;
        }

        if (question == null || question.isBlank()) {
            player.sendMessage(MessageUtil.parse(config.getMessageEmptyQuestion()));
            return;
        }

        if (!checkCooldown(player, config)) {
            return;
        }

        player.sendMessage(MessageUtil.parse(config.getMessageThinking()));

        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> processAsync(player, question.trim()))
                .schedule();
    }

    private boolean checkCooldown(Player player, PluginConfig config) {
        if (config.getCooldownSeconds() <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null) {
            long elapsed = (now - last) / 1000;
            long remaining = config.getCooldownSeconds() - elapsed;
            if (remaining > 0) {
                player.sendMessage(MessageUtil.format(config.getMessageCooldown(), "seconds", String.valueOf(remaining)));
                return false;
            }
        }

        cooldowns.put(player.getUniqueId(), now);
        return true;
    }

    private void processAsync(Player player, String question) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        UUID playerId = player.getUniqueId();

        try {
            String prompt = buildPrompt(playerId, question, config);
            String response = aiService.chat(config, prompt).join();
            String truncated = TextUtil.truncate(response, config.getMaxResponseLength());
            historyManager.addExchange(playerId, question, truncated);

            Component message = MessageUtil.parse(config.getMessagePrefix() + "<white>" + escapeMiniMessage(truncated) + "</white>");
            plugin.getServer().getScheduler()
                    .buildTask(plugin, () -> player.sendMessage(message))
                    .schedule();
        } catch (Exception e) {
            logger.error("AI 请求失败: {}", e.getMessage(), e);
            Component error = MessageUtil.format(config.getMessageError(), "error", escapeMiniMessage(e.getMessage()));
            plugin.getServer().getScheduler()
                    .buildTask(plugin, () -> player.sendMessage(error))
                    .schedule();
        }
    }

    private String buildPrompt(UUID playerId, String question, PluginConfig config) {
        StringBuilder prompt = new StringBuilder();

        if (config.isRagEnabled()) {
            List<DocumentChunk> chunks = knowledgeRetriever.retrieve(question, config.getMaxChunks());
            if (chunks.isEmpty() && documentStore.isEmpty()) {
                prompt.append("【服务器文档】\n（暂无文档）\n\n");
            } else if (!chunks.isEmpty()) {
                prompt.append("【服务器文档】\n");
                for (int i = 0; i < chunks.size(); i++) {
                    DocumentChunk chunk = chunks.get(i);
                    prompt.append("--- 片段 ").append(i + 1).append(" (").append(chunk.getSourceFile()).append(") ---\n");
                    prompt.append(chunk.getContent()).append("\n\n");
                }
            }
        }

        String history = historyManager.formatHistory(playerId, config.getMaxHistory());
        if (!history.isBlank()) {
            prompt.append("【对话历史】\n").append(history).append("\n");
        }

        prompt.append("【玩家问题】\n").append(question);
        return prompt.toString();
    }

    public int reloadDocs() throws Exception {
        return documentStore.reload();
    }

    private String escapeMiniMessage(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "\\<");
    }
}
