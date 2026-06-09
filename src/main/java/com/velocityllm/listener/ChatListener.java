package com.velocityllm.listener;

import com.velocityllm.ai.ChatService;
import com.velocityllm.compat.CrossServerChatCompat;
import com.velocityllm.config.PluginConfig;
import com.velocityllm.VelocityLLMPlugin;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

public final class ChatListener {

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
            if (!matchesPrefix(message, prefix)) {
                continue;
            }

            String content = message.substring(prefix.length()).trim();
            event.setResult(PlayerChatEvent.ChatResult.denied());
            CrossServerChatCompat.suppressBroadcastMessage(event, plugin.getLogger());
            chatService.ask(event.getPlayer(), content);
            return;
        }
    }

    private static boolean matchesPrefix(String message, String prefix) {
        if (!message.startsWith(prefix)) {
            return false;
        }
        return message.length() == prefix.length() || Character.isWhitespace(message.charAt(prefix.length()));
    }
}
