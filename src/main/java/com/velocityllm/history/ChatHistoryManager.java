package com.velocityllm.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatHistoryManager {

    private final Map<UUID, Deque<Exchange>> histories = new ConcurrentHashMap<>();

    public void addExchange(UUID playerId, String question, String answer) {
        histories.computeIfAbsent(playerId, id -> new ArrayDeque<>())
                .addLast(new Exchange(question, answer));
    }

    public String formatHistory(UUID playerId, int maxRounds) {
        Deque<Exchange> history = histories.get(playerId);
        if (history == null || history.isEmpty() || maxRounds <= 0) {
            return "";
        }

        ArrayDeque<Exchange> recent = new ArrayDeque<>(history);
        while (recent.size() > maxRounds) {
            recent.removeFirst();
        }

        StringBuilder builder = new StringBuilder();
        for (Exchange exchange : recent) {
            builder.append("玩家: ").append(exchange.question()).append('\n');
            builder.append("AI: ").append(exchange.answer()).append('\n');
        }
        return builder.toString().trim();
    }

    public void clear(UUID playerId) {
        histories.remove(playerId);
    }

    private record Exchange(String question, String answer) {
    }
}
