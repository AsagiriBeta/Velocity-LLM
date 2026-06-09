package com.velocityllm.listener;

import com.velocityllm.ai.ChatService;
import com.velocityllm.compat.CrossServerChatCompat;
import com.velocityllm.config.PluginConfig;
import com.velocityllm.VelocityLLMPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

/**
 * 在 {@code PlayerChatEvent} 上拦截 AI 触发消息。
 *
 * <p>兼容 Essential-PlayerInfo 等跨服聊天插件：它们在 priority 100 处广播聊天内容，
 * 且不会检查 {@code ChatResult.denied()}。因此在拒绝事件的同时清空消息文本，
 * 使后续广播不会将 @ai 提问内容发送到其他子服。普通聊天不受影响。
 */
public final class ChatListener {

    /** 高于 Essential-PlayerInfo Message 模块的 priority (100)，确保先处理。 */
    private static final int PRIORITY_ABOVE_CROSS_SERVER_CHAT = 101;

    private final VelocityLLMPlugin plugin;
    private final ChatService chatService;

    public ChatListener(VelocityLLMPlugin plugin, ChatService chatService) {
        this.plugin = plugin;
        this.chatService = chatService;
    }

    @Subscribe(priority = PRIORITY_ABOVE_CROSS_SERVER_CHAT)
    public void onPlayerChat(PlayerChatEvent event) {
        PluginConfig config = plugin.getConfigManager().getConfig();
        if (!config.isAiEnabled() || !config.isTriggerEnabled()) {
            return;
        }

        String message = event.getMessage();
        for (String prefix : config.getTriggerPrefixes()) {
            if (!message.startsWith(prefix)) {
                continue;
            }

            String question = message.substring(prefix.length()).trim();
            event.setResult(PlayerChatEvent.ChatResult.denied());
            CrossServerChatCompat.suppressBroadcastMessage(event, plugin.getLogger());
            chatService.ask(event.getPlayer(), question);
            return;
        }
    }
}
