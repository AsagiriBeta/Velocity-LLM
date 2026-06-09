package com.velocityllm.listener;

import com.velocityllm.ai.ChatService;
import com.velocityllm.config.PluginConfig;
import com.velocityllm.VelocityLLMPlugin;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;

public final class ChatListener {

    private final VelocityLLMPlugin plugin;
    private final ChatService chatService;

    public ChatListener(VelocityLLMPlugin plugin, ChatService chatService) {
        this.plugin = plugin;
        this.chatService = chatService;
    }

    @Subscribe(order = PostOrder.FIRST)
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
            chatService.ask(event.getPlayer(), question);
            return;
        }
    }
}
