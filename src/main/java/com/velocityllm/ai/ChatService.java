package com.velocityllm.ai;

import com.velocityllm.config.PluginConfig;
import com.velocityllm.rag.DocumentChunk;
import com.velocityllm.rag.RagService;
import com.velocityllm.util.MessageUtil;
import com.velocityllm.util.TextUtil;
import com.velocityllm.VelocityLLMPlugin;
import com.velocityllm.history.ChatHistoryManager;
import net.kyori.adventure.text.Component;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChatService {

    private final VelocityLLMPlugin plugin;
    private final AIService aiService;
    private final RagService ragService;
    private final ChatHistoryManager historyManager;
    private final Logger logger;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public ChatService(
            VelocityLLMPlugin plugin,
            AIService aiService,
            RagService ragService,
            ChatHistoryManager historyManager,
            Logger logger
    ) {
        this.plugin = plugin;
        this.aiService = aiService;
        this.ragService = ragService;
        this.historyManager = historyManager;
        this.logger = logger;
    }

    public void ask(Player player, String message) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        if (!config.isAiEnabled()) {
            player.sendMessage(MessageUtil.parse(config.getMessageDisabled()));
            return;
        }

        if (message == null || message.isBlank()) {
            player.sendMessage(MessageUtil.parse(config.getMessageEmptyInput()));
            return;
        }

        if (!checkCooldown(player, config)) {
            return;
        }

        String trimmed = message.trim();
        if (config.isShowPlayerMessages()) {
            deliverPlayerLine(player, config, trimmed);
        }

        player.sendMessage(MessageUtil.parse(config.getMessageThinking()));

        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> processAsync(player, trimmed))
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

    private void processAsync(Player player, String message) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        UUID playerId = player.getUniqueId();

        try {
            String userContent = buildUserContent(message, config);
            List<ChatMessage> history = historyManager.toMessages(playerId, config.getMaxHistory());
            String response = aiService.chat(config, history, userContent).join();
            String truncated = TextUtil.truncate(response, config.getMaxResponseLength());
            historyManager.addExchange(playerId, message, truncated);

            plugin.getServer().getScheduler()
                    .buildTask(plugin, () -> deliverAiResponse(player, config, truncated))
                    .schedule();
        } catch (Exception e) {
            logger.error("AI 请求失败: {}", e.getMessage(), e);
            Component error = MessageUtil.format(config.getMessageError(), "error", escapeMiniMessage(e.getMessage()));
            plugin.getServer().getScheduler()
                    .buildTask(plugin, () -> player.sendMessage(error))
                    .schedule();
        }
    }

    private String buildUserContent(String message, PluginConfig config) {
        if (!config.isRagEnabled()) {
            return message;
        }

        List<DocumentChunk> chunks = ragService.retrieve(message);
        if (chunks.isEmpty()) {
            if (ragService.isEmpty()) {
                return "【服务器文档】\n（暂无文档）\n\n【玩家消息】\n" + message;
            }
            return message;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("【服务器文档】\n");
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            builder.append("--- 片段 ").append(i + 1).append(" (").append(chunk.getSourceFile()).append(") ---\n");
            builder.append(chunk.getContent()).append("\n\n");
        }
        builder.append("【玩家消息】\n").append(message);
        return builder.toString();
    }

    private void deliverPlayerLine(Player player, PluginConfig config, String message) {
        String formatted = config.getMessagePrefix()
                + "<gray><" + escapeMiniMessage(player.getUsername()) + "></gray> "
                + "<white>" + escapeMiniMessage(message) + "</white>";
        deliverToAudience(player, config, List.of(MessageUtil.parse(formatted)));
    }

    private void deliverAiResponse(Player player, PluginConfig config, String response) {
        List<Component> messages = buildAiResponseComponents(config, response);
        deliverToAudience(player, config, messages);
    }

    private void deliverToAudience(Player player, PluginConfig config, List<Component> messages) {
        if (config.getResponseVisibility() == PluginConfig.ResponseVisibility.SAME_SERVER) {
            forEachSameServerPlayer(player, viewer -> deliverMessages(viewer, messages));
        } else {
            deliverMessages(player, messages);
        }
    }

    private void forEachSameServerPlayer(Player player, Consumer<Player> action) {
        player.getCurrentServer().ifPresentOrElse(
                connection -> {
                    RegisteredServer server = connection.getServer();
                    for (Player viewer : server.getPlayersConnected()) {
                        action.accept(viewer);
                    }
                },
                () -> action.accept(player)
        );
    }

    private void deliverMessages(Player player, List<Component> messages) {
        for (Component message : messages) {
            player.sendMessage(message);
        }
    }

    private List<Component> buildAiResponseComponents(PluginConfig config, String response) {
        List<Component> components = new ArrayList<>();
        String prefix = config.getMessagePrefix();
        List<String> lines = splitLines(response, 220);
        for (int i = 0; i < lines.size(); i++) {
            String linePrefix = i == 0 ? prefix : "";
            String formatted = linePrefix + "<white>" + escapeMiniMessage(lines.get(i)) + "</white>";
            components.add(MessageUtil.parse(formatted));
        }
        return components;
    }

    private List<String> splitLines(String text, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        String[] paragraphs = text.split("\\R");
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            while (trimmed.length() > maxLineLength) {
                lines.add(trimmed.substring(0, maxLineLength));
                trimmed = trimmed.substring(maxLineLength);
            }
            lines.add(trimmed);
        }
        return lines;
    }

    public int reload() throws Exception {
        return ragService.reload();
    }

    private String escapeMiniMessage(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "\\<");
    }
}
