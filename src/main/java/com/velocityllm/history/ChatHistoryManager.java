package com.velocityllm.history;

import com.velocityllm.ai.ChatMessage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatHistoryManager {

    private final Map<UUID, Deque<Exchange>> histories = new ConcurrentHashMap<>();

    public void addExchange(UUID playerId, String question, String answer) {
        histories.computeIfAbsent(playerId, id -> new ArrayDeque<>())
                .addLast(new Exchange(question, answer));
    }

    public List<ChatMessage> toMessages(UUID playerId, int maxRounds) {
        Deque<Exchange> history = histories.get(playerId);
        if (history == null || history.isEmpty() || maxRounds <= 0) {
            return List.of();
        }

        ArrayDeque<Exchange> recent = new ArrayDeque<>(history);
        while (recent.size() > maxRounds) {
            recent.removeFirst();
        }

        List<ChatMessage> messages = new ArrayList<>(recent.size() * 2);
        for (Exchange exchange : recent) {
            messages.add(new ChatMessage("user", exchange.question()));
            messages.add(new ChatMessage("assistant", exchange.answer()));
        }
        return messages;
    }

    public void clear(UUID playerId) {
        histories.remove(playerId);
    }

    private record Exchange(String question, String answer) {
    }
}
