package com.velocityllm.listener;

import com.velocityllm.history.ChatHistoryManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;

public final class DisconnectListener {

    private final ChatHistoryManager historyManager;

    public DisconnectListener(ChatHistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        historyManager.clear(event.getPlayer().getUniqueId());
    }
}
